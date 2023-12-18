package mobappdev.example.sensorapplication.data

/**
 * File: InternalSensorControllerImpl.kt
 * Purpose: Implementation of the Internal Sensor Controller.
 * Author:
 * Created: 2023-09-21
 * Last modified: 2023-09-21
 */

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.util.Log
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.DelicateCoroutinesApi
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.domain.InternalSensorController
import javax.inject.Inject

private const val LOG_TAG = "InternalSensorController"
private const val UPDATE_DELAY = 5L

class InternalSensorControllerImpl @Inject constructor(
    context: Context,
    private val calculationModel: CalculationModel,
) : InternalSensorController, SensorEventListener {

    private val _angleMeasurementCurrent = MutableStateFlow<AngleMeasurements.Measurement?>(null)
    override val angleMeasurementCurrent: StateFlow<AngleMeasurements.Measurement?>
        get() = _angleMeasurementCurrent.asStateFlow()

    private val _angleMeasurements = MutableStateFlow<AngleMeasurements?>(null)
    override val angleMeasurements: StateFlow<AngleMeasurements?>
        get() = _angleMeasurements.asStateFlow()


    // Expose acceleration to the UI
    private val _currentLinAccUI = MutableStateFlow<Triple<Float, Float, Float>?>(null)
    override val currentLinAccUI: StateFlow<Triple<Float, Float, Float>?>
        get() = _currentLinAccUI.asStateFlow()

    //private var _currentGyro: Triple<Float, Float, Float>? = null

    private var _currentGyro: MeasurementData? = null
    private var _currentLinAcc: MeasurementData? = null

    // Expose gyro to the UI on a certain interval
    private val _currentGyroUI = MutableStateFlow<Triple<Float, Float, Float>?>(null)
    override val currentGyroUI: StateFlow<Triple<Float, Float, Float>?>
        get() = _currentGyroUI.asStateFlow()

    private val _streamingGyro = MutableStateFlow(false)
    override val streamingGyro: StateFlow<Boolean>
        get() = _streamingGyro.asStateFlow()

    //private var _currentLinAcc: Triple<Float, Float, Float>? = null

    private val _streamingLinAcc = MutableStateFlow(false)
    override val streamingLinAcc: StateFlow<Boolean>
        get() = _streamingLinAcc.asStateFlow()

    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager
    private val gyroSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE)
    }

    private val imuSensor: Sensor? by lazy {
        sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun startImuStream() {
        if (imuSensor == null) {
            Log.e(LOG_TAG, "Imu sensor is not available on this device")
            return
        }
        if (_streamingLinAcc.value) {
            Log.e(LOG_TAG, "Imu sensor is already streaming")
            return
        }
        Log.e(LOG_TAG, "Imu stream started")
        sensorManager.registerListener(this, imuSensor, SensorManager.SENSOR_DELAY_UI)
        GlobalScope.launch(Dispatchers.Main) {
            _streamingLinAcc.value = true
            while (_streamingLinAcc.value) {
                delay(UPDATE_DELAY)
                if (_currentLinAcc != null) {  //TODO: Om appen hänger sig, ta bort denna. Detta är ett test
                    val angle = calculationModel.getLinearAccelerationAngle(_currentLinAcc!!.axisValues)
                    val angleWithTimestamp =
                        AngleMeasurements.Measurement(angle, _currentLinAcc!!.timestamp)
                    updateAngleValues(angleWithTimestamp)
                } else {
                    Log.d(LOG_TAG, "Null")
                }


            }
        }

    }

    override fun stopImuStream() {
        if (_streamingLinAcc.value) {
            Log.d(LOG_TAG, "Stopping Imu Stream")
            _currentLinAccUI.update { null }
            sensorManager.unregisterListener(this, imuSensor)
            _streamingLinAcc.value = false
        }
    }

    override fun startDualStream() {
        if (gyroSensor == null || imuSensor == null) {
            Log.e(LOG_TAG, "Sensor not found on device")
            return
        }
        if (_streamingGyro.value || _streamingLinAcc.value) {
            Log.e(LOG_TAG, "Sensors are active")
            return
        }
        Log.e(LOG_TAG, "Starting dual stream")
        val coroutineScope = CoroutineScope(Dispatchers.Main)
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, imuSensor, SensorManager.SENSOR_DELAY_UI)
        coroutineScope.launch(Dispatchers.Main) {
            _streamingGyro.value = true
            _streamingLinAcc.value = true
            while (_streamingLinAcc.value && _streamingGyro.value) {
                delay(UPDATE_DELAY)
                val angle = calculationModel.getLinearAccelerationAngleWithGyroFilter(
                    _currentLinAcc!!.axisValues, _currentGyro!!.axisValues
                )
                val angleWithTimestamp = AngleMeasurements.Measurement(angle, _currentGyro!!.timestamp)
                updateAngleValues(angleWithTimestamp)
            }
        }
    }

    @OptIn(DelicateCoroutinesApi::class)
    override fun startGyroStream() {
        /*
        if (gyroSensor == null) {
            Log.e(LOG_TAG, "Gyroscope sensor is not available on this device")
            return
        }
        if (_streamingGyro.value) {
            Log.e(LOG_TAG, "Gyroscope sensor is already streaming")
            return
        }
        // Register this class as a listener for gyroscope events
        sensorManager.registerListener(this, gyroSensor, SensorManager.SENSOR_DELAY_UI)
        // Start a coroutine to update the UI variable on a 2 Hz interval
        GlobalScope.launch(Dispatchers.Main) {
            _streamingGyro.value = true
            while (_streamingGyro.value) {
                delay(1000)
            }
        }
*/
    }

    override fun stopGyroStream() {
        if (_streamingGyro.value) {
            // Unregister the listener to stop receiving gyroscope events (automatically stops the coroutine as well
            Log.d(LOG_TAG, "Stopping Gyro Stream")
            _currentGyroUI.update { null }
            sensorManager.unregisterListener(this, gyroSensor)
            _streamingGyro.value = false
        }
    }

    override fun onSensorChanged(event: SensorEvent) {
        if (event.sensor.type == Sensor.TYPE_GYROSCOPE) {
            _currentGyro = MeasurementData(
                Triple(event.values[0], event.values[1], event.values[2]), event.timestamp
            )
        }
        if (event.sensor.type == Sensor.TYPE_ACCELEROMETER) {
            _currentLinAcc = MeasurementData(
                Triple(event.values[0], event.values[1], event.values[2]), event.timestamp
            )
        }
    }

    private fun updateAngleValues(angleMeasurements: AngleMeasurements.Measurement) {
        _angleMeasurementCurrent.value = angleMeasurements // UI update
        // Create a new mutable list from the existing list and add the new measurement
        val updatedList = _angleMeasurements.value?.list?.toMutableList() ?: mutableListOf()
        updatedList.add(angleMeasurements)
        val updatedAngleMeasurements = AngleMeasurements(updatedList)
        // Update _angleMeasurements with the new object
        _angleMeasurements.value = updatedAngleMeasurements

    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        // Not used in this example
    }


}

data class MeasurementData(
    val axisValues: Triple<Float, Float, Float>, val timestamp: Long
)