package mobappdev.example.sensorapplication.data

/**
 * File: AndroidPolarController.kt
 * Purpose: Implementation of the PolarController Interface.
 *          Communicates with the polar API
 * Author:
 * Created: 2023-07-08
 * Last modified: 2023-07-11
 */

import android.content.Context
import android.util.Log
import com.polar.sdk.api.PolarBleApi
import com.polar.sdk.api.PolarBleApiCallback
import com.polar.sdk.api.PolarBleApiDefaultImpl
import com.polar.sdk.api.errors.PolarInvalidArgument
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarDeviceInfo
import com.polar.sdk.api.model.PolarGyroData
import com.polar.sdk.api.model.PolarHrData
import com.polar.sdk.api.model.PolarSensorSetting
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.Scheduler
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.domain.PolarController
import java.util.UUID

class AndroidPolarController (
    private val context: Context,
    private val calculationModel: CalculationModel
): PolarController {

    private val api: PolarBleApi by lazy {
        // Notice all features are enabled
        PolarBleApiDefaultImpl.defaultImplementation(
            context = context,
            setOf(
                PolarBleApi.PolarBleSdkFeature.FEATURE_HR,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_SDK_MODE,
                PolarBleApi.PolarBleSdkFeature.FEATURE_BATTERY_INFO,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_H10_EXERCISE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_OFFLINE_RECORDING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_ONLINE_STREAMING,
                PolarBleApi.PolarBleSdkFeature.FEATURE_POLAR_DEVICE_TIME_SETUP,
                PolarBleApi.PolarBleSdkFeature.FEATURE_DEVICE_INFO
            )
        )
    }

    private var scanDisposable: Disposable? = null
    private var hrDisposable: Disposable? = null
    private var accDisposable: Disposable? = null
    private var gyrDisposable: Disposable? = null
    private val TAG = "AndroidPolarController"

    private val _hrCurrent = MutableStateFlow<Int?>(null)
    override val hrCurrent: StateFlow<Int?>
        get() = _hrCurrent.asStateFlow()


    override val angleMeasurements: StateFlow<AngleMeasurements> = calculationModel.angleMeasurementsFlow
    override val angleMeasurementCurrent: StateFlow<AngleMeasurements.measurment> = calculationModel.angleMeasurementLastFlow



    private val _devicesFlow = MutableSharedFlow<PolarDeviceInfo>()
    override val devicesFlow: Flow<PolarDeviceInfo> = _devicesFlow.asSharedFlow()


    private val _hrList = MutableStateFlow<List<Int>>(emptyList())
    override val hrList: StateFlow<List<Int>>
        get() = _hrList.asStateFlow()


    private val _accList = MutableStateFlow<PolarAccelerometerData?>(null)
    override val accList: StateFlow<PolarAccelerometerData?>
        get() = _accList.asStateFlow()

    private val _accCurrent = MutableStateFlow<PolarAccelerometerData.PolarAccelerometerDataSample?>(null)
    override val accCurrent: StateFlow<PolarAccelerometerData.PolarAccelerometerDataSample?>
        get() = _accCurrent.asStateFlow()


    private val _gyrList = MutableStateFlow<PolarGyroData?>(null)
    override val gyrList: StateFlow<PolarGyroData?>
        get() = _gyrList.asStateFlow()

    private val _gyrCurrent = MutableStateFlow<PolarGyroData.PolarGyroDataSample?>(null)
    override val gyrCurrent: StateFlow<PolarGyroData.PolarGyroDataSample?>
        get() = _gyrCurrent.asStateFlow()


    private val _connected = MutableStateFlow(false)
    override val connected: StateFlow<Boolean>
        get() = _connected.asStateFlow()

    private val _measuring = MutableStateFlow(false)
    override val measuring: StateFlow<Boolean>
        get() = _measuring.asStateFlow()

    init {
        api.setPolarFilter(false)
        val enableSdkLogs = false
        if(enableSdkLogs) {
            api.setApiLogger { s: String -> Log.d("Polar API Logger", s) }
        }

        api.setApiCallback(object: PolarBleApiCallback() {
            override fun batteryLevelReceived(identifier: String, level: Int) {
                Log.d(TAG, "BATTERY LEVEL: $level")
            }

            override fun deviceConnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTED: ${polarDeviceInfo.deviceId}")
                _connected.update { true }
            }

            override fun deviceConnecting(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "CONNECTING: ${polarDeviceInfo.deviceId}")
            }

            override fun deviceDisconnected(polarDeviceInfo: PolarDeviceInfo) {
                Log.d(TAG, "DISCONNECTED: ${polarDeviceInfo.deviceId}")
                _connected.update { false }
            }

            override fun disInformationReceived(identifier: String, uuid: UUID, value: String) {
                Log.d(TAG, "DIS INFO uuid: $uuid value: $value")
            }
        })
    }

    override fun connectToDevice(deviceId: String) {
        try {
            api.connectToDevice(deviceId)
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e(TAG, "Failed to connect to $deviceId.\n Reason $polarInvalidArgument")
        }
    }

    override fun disconnectFromDevice(deviceId: String) {
        try {
            api.disconnectFromDevice(deviceId)
        } catch (polarInvalidArgument: PolarInvalidArgument) {
            Log.e(TAG, "Failed to disconnect from $deviceId.\n Reason $polarInvalidArgument")
        }
    }

    override fun startHrStreaming(deviceId: String) {
        val isDisposed = hrDisposable?.isDisposed ?: true
        if(isDisposed) {
            _measuring.update { true }
            hrDisposable = api.startHrStreaming(deviceId)
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { hrData: PolarHrData ->
                        for (sample in hrData.samples) {
                            _hrCurrent.update { sample.hr }
                            _hrList.update { hrList ->
                                hrList + sample.hr
                            }
                            Log.d(TAG, sample.hr.toString())
                        }
                    },
                    { error: Throwable ->
                        Log.e(TAG, "Hr stream failed.\nReason $error")
                    },
                    { Log.d(TAG, "Hr stream complete")}
                )
        } else {
            Log.d(TAG, "Already streaming")
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun searchBTDevices() {
        Log.d(TAG, "Searching")
        val isDisposed = scanDisposable?.isDisposed ?: true
        if (isDisposed) {
            //toggleButtonDown(scanButton, R.string.scanning_devices)
            scanDisposable = api.searchForDevice()
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarDeviceInfo: PolarDeviceInfo ->
                        GlobalScope.launch {
                            _devicesFlow.emit(polarDeviceInfo)
                        }
                        Log.d(TAG, "polar device found id: " + polarDeviceInfo.deviceId + " address: " + polarDeviceInfo.address + " rssi: " + polarDeviceInfo.rssi + " name: " + polarDeviceInfo.name + " isConnectable: " + polarDeviceInfo.isConnectable)
                    },
                    { error: Throwable ->
                        //toggleButtonUp(scanButton, "Scan devices")
                        Log.e(TAG, "Device scan failed. Reason $error")
                    },
                    {
                        //toggleButtonUp(scanButton, "Scan devices")
                        Log.d(TAG, "complete")
                    }
                )
        } else {
            //toggleButtonUp(scanButton, "Scan devices")
            scanDisposable?.dispose()
        }


    }

    override fun stopHrStreaming() {
        _measuring.update { false }
        hrDisposable?.dispose()
        _hrCurrent.update { null }
    }

    override fun startAccStream(deviceId: String){
        val isDisposed = accDisposable?.isDisposed ?: true
        if (isDisposed) {
            accDisposable = requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.ACC)
                .flatMap { settings: PolarSensorSetting ->
                    api.startAccStreaming(deviceId, settings)
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(
                    { polarAccelerometerData: PolarAccelerometerData ->
                        for (data in polarAccelerometerData.samples) {
                            _accCurrent.update { data }
                            //Log.d(TAG, "ACC in currentAcc: " +currentAcc.value)
                            _accList.update { currentData ->
                                val newSamples = currentData?.samples.orEmpty() + data
                                PolarAccelerometerData(newSamples, data.timeStamp)
                            }
                            var angleMeasurements:AngleMeasurements.measurment=calculationModel.getLinearAccelerationAngle(Triple(data.x.toFloat(),data.y.toFloat(),data.z.toFloat()), data.timeStamp)
                            Log.d(TAG, "angle: "+angleMeasurements.angle.toString() + ", time: " + angleMeasurements.timestamp.toString())
                            //TODO: add to list
                            Log.d(TAG, "ACC    x: ${data.x} y: ${data.y} z: ${data.z} timeStamp: ${data.timeStamp}")
                        }
                    },
                    { error: Throwable ->
                        //toggleButtonUp(accButton, R.string.start_acc_stream)
                        Log.e(TAG, "ACC stream failed. Reason $error")
                    },
                    {
                        //showToast("ACC stream complete")
                        Log.d(TAG, "ACC stream complete")
                    }
                )
        } else {
            //toggleButtonUp(accButton, R.string.start_acc_stream)
            // NOTE dispose will stop streaming if it is "running"
            //accDisposable?.dispose()
            Log.d(TAG, "ACC Already streaming")
        }
    }

    override fun stopAccStreaming() {
        _measuring.update { false }
        accDisposable?.dispose()
        _accCurrent.update { null }
    }

    override fun startGyroStream(deviceId: String){
        val isDisposed = gyrDisposable?.isDisposed ?: true
        if (isDisposed) {
            gyrDisposable =
                requestStreamSettings(deviceId, PolarBleApi.PolarDeviceDataType.GYRO)
                    .flatMap { settings: PolarSensorSetting ->
                        api.startGyroStreaming(deviceId, settings)
                    }
                    .observeOn(AndroidSchedulers.mainThread())
                    .subscribe(
                        { polarGyroData: PolarGyroData ->
                            for (data in polarGyroData.samples) {
                                _gyrCurrent.update { data }
                                Log.d(TAG, "ACC degrees: " + calculationModel.getLinearAccelerationAngle(Triple(data.x,data.y,data.z),data.timeStamp).toString() )
                                _gyrList.update { currentData ->
                                    val newSamples = currentData?.samples.orEmpty() + data
                                    PolarGyroData(newSamples, data.timeStamp)
                                }
                                Log.d(TAG, "GYR    x: ${data.x} y: ${data.y} z: ${data.z} timeStamp: ${data.timeStamp}")
                            }
                        },
                        { error: Throwable ->
                            Log.e(TAG, "GYR stream failed. Reason $error")
                        },
                        { Log.d(TAG, "GYR stream complete") }
                    )
        } else {
            // NOTE dispose will stop streaming if it is "running"
            gyrDisposable?.dispose()
        }
    }

    override fun stopGyroStreaming() {
        _measuring.update { false }
        gyrDisposable?.dispose()
        _gyrCurrent.update { null }
    }

    private fun requestStreamSettings(identifier: String, feature: PolarBleApi.PolarDeviceDataType): Flowable<PolarSensorSetting> {
        val availableSettings = api.requestStreamSettings(identifier, feature)
        val allSettings = api.requestFullStreamSettings(identifier, feature)
            .onErrorReturn { error: Throwable ->
                Log.w(TAG, "Full stream settings are not available for feature $feature. REASON: $error")
                PolarSensorSetting(emptyMap())
            }
        return Single.zip(availableSettings, allSettings) { available: PolarSensorSetting, all: PolarSensorSetting ->
            if (available.settings.isEmpty()) {
                throw Throwable("Settings are not available")
            } else {
                Log.d(TAG, "Feature " + feature + " available settings " + available.settings)
                Log.d(TAG, "Feature " + feature + " all settings " + all.settings)
                return@zip android.util.Pair(available, all)
            }
        }
            .observeOn(AndroidSchedulers.mainThread())
            .toFlowable()
            .flatMap { sensorSettings: android.util.Pair<PolarSensorSetting, PolarSensorSetting> ->
                DialogUtility.showAllSettingsDialog(
                    sensorSettings.first.settings,
                    sensorSettings.second.settings
                ).toFlowable()
            }
    }


}

