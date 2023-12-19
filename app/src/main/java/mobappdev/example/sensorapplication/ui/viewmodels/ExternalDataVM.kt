package mobappdev.example.sensorapplication.ui.viewmodels

/**
 * File: DataVM.kt
 * Purpose: Defines the view model of the data screen.
 *          Uses Dagger-Hilt to inject a controller model
 * Author:
 * Created: 2023-07-08
 * Last modified:
 */

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polar.sdk.api.model.PolarDeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.data.AngleMeasurements
import mobappdev.example.sensorapplication.data.CsvExporter
import mobappdev.example.sensorapplication.domain.PolarController
import mobappdev.example.sensorapplication.persistence.MeasurementType
import mobappdev.example.sensorapplication.persistence.MeasurementsRepository
import mobappdev.example.sensorapplication.ui.shared.Canvas
import mobappdev.example.sensorapplication.ui.shared.Helpers
import mobappdev.example.sensorapplication.ui.shared.TimerValues
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.floor

private const val LOG_TAG = "DataVM"

@HiltViewModel
class ExternalDataVM @Inject constructor(
    private val polarController: PolarController,
    private val csvExporter: CsvExporter,
    private val measurementsRepository: MeasurementsRepository
) : ViewModel() {

    private val _offsets = MutableStateFlow<List<Offset>>(emptyList())
    val offsets: StateFlow<List<Offset>> = _offsets
    private val _currentAngle = MutableStateFlow(
        AngleMeasurements.Measurement()
    )
    val currentAngle: StateFlow<AngleMeasurements.Measurement> = _currentAngle
    private val measurements: MutableList<AngleMeasurements.Measurement> = mutableListOf()
    private val _deviceList = MutableStateFlow<List<PolarDeviceInfo>>(listOf())
    val deviceList: StateFlow<List<PolarDeviceInfo>>
        get() = _deviceList.asStateFlow()
    private val _state = MutableStateFlow(DataUiState())
    val state = combine(
        polarController.connected, _state
    ) { connected, state ->
        state.copy(
            connected = connected,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)

    private var timer: Job? = null
    private var streamType: StreamType? = null
    private val _deviceId = MutableStateFlow("")


    val deviceId: StateFlow<String>
        get() = _deviceId.asStateFlow()


    fun chooseSensor(deviceId: String) {
        _deviceId.update { deviceId }
    }

    private fun connectToSensor() {
        polarController.connectToDevice(_deviceId.value)
    }

    fun chooseSensorAndConnect(deviceId: String) {
        _deviceId.update { deviceId }
        connectToSensor()
        closeBluetoothDialog()
    }

    fun disconnectFromSensor() {
        stopDataStream()
        polarController.disconnectFromDevice(_deviceId.value)
    }
    fun exportData() {
        csvExporter.exportMeasurements(measurements)
    }

    fun startMeasurement(){
        measurements.clear()
        _state.update { it.copy(startTime = -1L, showSaveButton = false) }
        if (state.value.dualMeasurement){
            startExtAccAndGyro()
        } else {
            startExtAcc()
        }
    }

    private fun startExtAccAndGyro() {
        polarController.startAccAndGyroStream(_deviceId.value)
        streamType = StreamType.FOREIGN_ACC_AND_GYRO
        _state.update { it.copy(measuring = true) }
    }

    private fun startExtAcc() {
        polarController.startAccStream(_deviceId.value)
        streamType = StreamType.FOREIGN_ACC
        _state.update { it.copy(measuring = true) }
    }

    fun startExtGyro() {
        polarController.startGyroStream(_deviceId.value)
        streamType = StreamType.FOREIGN_GYRO
        _state.update { it.copy(measuring = true) }
    }

    fun stopDataStream() {
        when (streamType) {
            StreamType.FOREIGN_HR -> polarController.stopHrStreaming()
            StreamType.FOREIGN_ACC -> polarController.stopAccStreaming()
            StreamType.FOREIGN_GYRO -> polarController.stopGyroStreaming()
            StreamType.FOREIGN_ACC_AND_GYRO -> {
                polarController.stopAccStreaming(); polarController.stopAccStreaming()
            }

            else -> {} // Do nothing
        }
        _state.update { it.copy(measuring = false) }
        stopCounter()
        _state.update { it.copy(
            showSaveButton = true,
            countDownTimer = _state.value.selectedTimerValue.toInt()
        ) }

        Log.d(LOG_TAG, "Stream stopped.")
    }
    private fun deviceInList(polarDeviceInfo: PolarDeviceInfo): Boolean {
        for (element in _deviceList.value) {
            if (polarDeviceInfo.deviceId == element.deviceId) {
                return true
            }
        }
        return false
    }

    private fun searchBTDevices() {
        viewModelScope.launch {
            if (!state.value.isSearching) {
                _deviceList.value = emptyList()
            }
            _state.value = _state.value.copy(isSearching = polarController.searchBTDevices())
            Log.d(LOG_TAG, "IsSearching: ${_state.value.isSearching}")
        }
    }

    fun openBluetoothDialog() {
        _state.value = state.value.copy(showDialog = true)
        searchBTDevices()
    }

    fun closeBluetoothDialog() {
        _state.value = _state.value.copy(showDialog = false)
        searchBTDevices()
    }

    fun setSingleMeasurement() {
        _state.value = _state.value.copy(dualMeasurement = false)
    }

    fun setDualMeasurement() {
        _state.value = state.value.copy(dualMeasurement = true)
    }

    private fun stopCounter() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }
    private fun startCounter() {
        if (timer == null) {
            _state.update { it.copy(timeInMs = 0) }
            timer = viewModelScope.launch {
                var counter = 0
                while (_state.value.countDownTimer > 0) {
                    delay(TimerValues.UPDATE_TIME)
                    _state.update { it.copy(timeInMs = _state.value.timeInMs + TimerValues.UPDATE_TIME) }
                    counter++
                    if (counter == 10) {
                        _state.update { it.copy(countDownTimer = _state.value.countDownTimer - 1) }
                        counter = 0
                    }
                }
                stopDataStream()
            }
        }
    }


    fun saveToDb() {
        val timeMeasured = _state.value.timeInMs
        if (timeMeasured <= 0.5) {
            return
        }
        viewModelScope.launch {
            val dateTime: LocalDateTime = LocalDateTime.parse(_state.value.streamStarted)
            val measurementType: MeasurementType = MeasurementType.EXTERNAL
            measurementsRepository.insertMeasurements(
                measurements = measurements,
                type = measurementType,
                dateTime = dateTime,
                timeMeasured = timeMeasured
            )
            Log.d(LOG_TAG, measurementsRepository.getSize().toString())

            _state.update { it.copy(showSaveButton = false) }

        }
    }

    private fun addToOffsets(measurement: AngleMeasurements.Measurement) {
        if (timer == null) {
            startCounter()
            _state.update { it.copy(streamStarted = Helpers.getFormattedDateTimeNow()) }
            Log.d(LOG_TAG, "Counting up")
        }
        val yValue = Canvas.convertAngleToY(_state.value.canvasHeight, measurement.angle)
        val xValue = Canvas.convertTimestampToX(measurement.timestamp, _state.value.selectedTimerValue, _state.value.startTime)
        if (xValue >= _state.value.canvasWidth && floor(_state.value.selectedTimerValue) > 30 || xValue < 0) {
            _offsets.update { emptyList() }
            _state.update { it.copy(startTime = measurement.timestamp) }
        } else {
            _offsets.update { _offsets.value + Offset(xValue, yValue) }
        }
        //Log.d(LOG_TAG, "List size: " + _offsets.value.size.toString() + "| X: " + String.format("%.1f",xValue) + "| Y: " + yValue)
    }

    fun setCanvasDimension(canvasWidth: Float, canvasHeight: Float) {
        _state.update { it.copy(canvasWidth = canvasWidth, canvasHeight = canvasHeight) }
    }

    fun setTimerValue(timerValue: Float) {
        _state.update { it.copy(selectedTimerValue = timerValue, countDownTimer = timerValue.toInt()) }
    }

    init {
        viewModelScope.launch {
            launch {
                polarController.foundDevices.collect {
                    if (!deviceInList(it)) {
                        _deviceList.value = (_deviceList.value + it).toMutableList()
                    }
                }
            }
            launch {
                polarController.angleMeasurementCurrent.distinctUntilChangedBy {
                    it?.timestamp
                }.collect {
                    if (it != null) {
                        _currentAngle.value = it
                        addToOffsets(it)
                        measurements.add(it)
                    }
                }


            }
        }
    }
}

data class DataUiState(
    val connected: Boolean = false,
    val measuring: Boolean = false,
    val showDialog: Boolean = false,
    val isSearching: Boolean = false,
    val dualMeasurement: Boolean = false,
    val showSaveButton: Boolean = false,
    val canvasWidth: Float = 1000F,
    val canvasHeight: Float = 1000F,
    val selectedTimerValue: Float = TimerValues.MIN_TIMER.toFloat(),
    val countDownTimer: Int = TimerValues.MIN_TIMER,
    val startTime: Long = -1L,
    val timeInMs: Long = 0L,
    val streamStarted: String = "",
)

private enum class StreamType {
    FOREIGN_HR, FOREIGN_ACC, FOREIGN_GYRO, FOREIGN_ACC_AND_GYRO
}