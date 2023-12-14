package mobappdev.example.sensorapplication.ui.viewmodels

/**
 * File: DataVM.kt
 * Purpose: Defines the view model of the data screen.
 *          Uses Dagger-Hilt to inject a controller model
 * Author: Jitse van Esch
 * Created: 2023-07-08
 * Last modified: 2023-07-11
 */

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polar.sdk.api.model.PolarAccelerometerData
import com.polar.sdk.api.model.PolarGyroData
import com.polar.sdk.api.model.PolarDeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.domain.InternalSensorController
import mobappdev.example.sensorapplication.domain.PolarController
import javax.inject.Inject

private const val LOG_TAG = "DataVM"
@HiltViewModel
class DataVM @Inject constructor(
    private val polarController: PolarController,
    private val internalSensorController: InternalSensorController
): ViewModel() {

    private val gyroDataFlow = internalSensorController.currentGyroUI
    private val linAccDataFlow = internalSensorController.currentLinAccUI
    private val hrDataFlow = polarController.hrCurrent
    private val externalLinAccDataFlow = polarController.accCurrent
    private val externalGyroDataFlow = polarController.gyrCurrent

    private val _deviceList = MutableStateFlow<MutableList<PolarDeviceInfo>>(mutableListOf())
    val deviceList: StateFlow<List<PolarDeviceInfo>> = _deviceList;

    // Combine the two data flows
    val combinedDataFlow= combine(
        gyroDataFlow,
        hrDataFlow,
        linAccDataFlow,
        externalLinAccDataFlow,
        externalGyroDataFlow
    ) { gyro, hr, linAcc, externalLinAcc, externalGyro ->
        if (hr != null ) {
            CombinedSensorData.HrData(hr)
        } else if (gyro != null) {
            CombinedSensorData.GyroData(gyro)
        } else if (linAcc != null){
            CombinedSensorData.LinAccData(linAcc)
        } else if(externalLinAcc!=null){
            CombinedSensorData.ExternalLinAccData(externalLinAcc)
        } else if(externalGyro!=null){
            CombinedSensorData.ExternalGyroData(externalGyro)
        } else {
            null
        }
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), null)

    private val _state = MutableStateFlow(DataUiState())
    val state = combine(
        polarController.hrList,
        polarController.connected,
        _state
    ) { hrList, connected, state ->
        state.copy(
            hrList = hrList,
            connected = connected,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private var streamType: StreamType? = null


    private val _deviceId = MutableStateFlow("")
    val deviceId: StateFlow<String>
        get() = _deviceId.asStateFlow()


    fun chooseSensor(deviceId: String) {
        _deviceId.update { deviceId }
    }

    fun connectToSensor() {
        polarController.connectToDevice(_deviceId.value)
    }

    fun searchBTDevices() {
        polarController.searchBTDevices()
        viewModelScope.launch {
            polarController.devicesFlow.collect { newDevice ->
                val updatedList = _deviceList.value.toMutableList().apply {
                    add(newDevice)
                }
                _deviceList.value = updatedList
                Log.d(LOG_TAG, newDevice.name) // This prints the correct name
            }
        }
    }


    fun disconnectFromSensor() {
        stopDataStream()
        polarController.disconnectFromDevice(_deviceId.value)
    }

    fun startHr() {
        polarController.startHrStreaming(_deviceId.value)
        streamType = StreamType.FOREIGN_HR
        _state.update { it.copy(measuring = true) }
    }

    fun startExtAcc() {
        polarController.startAccStream(_deviceId.value)
        streamType = StreamType.FOREIGN_ACC
        _state.update { it.copy(measuring = true) }
    }

    fun startExtGyro() {
        polarController.startGyroStream(_deviceId.value)
        streamType = StreamType.FOREIGN_GYRO
        _state.update { it.copy(measuring = true) }
    }

    fun startGyro() {
        internalSensorController.startGyroStream()
        streamType = StreamType.LOCAL_GYRO

        _state.update { it.copy(measuring = true) }
    }

    fun startLinAcc() {
        internalSensorController.startImuStream()
        streamType = StreamType.LOCAL_ACC
        _state.update { it.copy(measuring = true) }
    }

    fun stopDataStream(){
        when (streamType) {
            StreamType.LOCAL_GYRO -> internalSensorController.stopGyroStream()
            StreamType.LOCAL_ACC -> internalSensorController.stopImuStream()
            StreamType.FOREIGN_HR -> polarController.stopHrStreaming()
            StreamType.FOREIGN_ACC -> polarController.stopAccStreaming()
            StreamType.FOREIGN_GYRO -> polarController.stopGyroStreaming()
            else -> {} // Do nothing
        }
        _state.update { it.copy(measuring = false) }
    }
}

data class DataUiState(
    val hrList: List<Int> = emptyList(),
    val connected: Boolean = false,
    val measuring: Boolean = false
)

enum class StreamType {
    LOCAL_GYRO, LOCAL_ACC, FOREIGN_HR, FOREIGN_ACC, FOREIGN_GYRO
}

sealed class CombinedSensorData {
    data class GyroData(val gyro: Triple<Float, Float, Float>?) : CombinedSensorData()
    data class HrData(val hr: Int?) : CombinedSensorData()
    data class LinAccData(val linAcc: Triple<Float, Float, Float>?) : CombinedSensorData()
    data class ExternalLinAccData(val extLinAcc: PolarAccelerometerData.PolarAccelerometerDataSample?) : CombinedSensorData()
    data class ExternalGyroData(val extGyro: PolarGyroData.PolarGyroDataSample?) : CombinedSensorData()

}