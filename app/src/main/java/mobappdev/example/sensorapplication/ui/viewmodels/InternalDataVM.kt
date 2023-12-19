package mobappdev.example.sensorapplication.ui.viewmodels


import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.data.AngleMeasurements
import mobappdev.example.sensorapplication.data.CsvExporter
import mobappdev.example.sensorapplication.domain.InternalSensorController
import mobappdev.example.sensorapplication.persistence.MeasurementType
import mobappdev.example.sensorapplication.persistence.MeasurementsRepository
import mobappdev.example.sensorapplication.ui.shared.Canvas
import mobappdev.example.sensorapplication.ui.shared.Helpers
import mobappdev.example.sensorapplication.ui.shared.TimerValues
import java.time.LocalDateTime
import javax.inject.Inject
import kotlin.math.floor

private const val TAG = "InternalDataVM"

@HiltViewModel
class InternalDataVM @Inject constructor(
    private val internalSensorController: InternalSensorController,

    private val csvExporter: CsvExporter,

    private val measurementsRepository: MeasurementsRepository

) : ViewModel() {

    private var timer: Job? = null

    private val measurements: MutableList<AngleMeasurements.Measurement> = mutableListOf()

    private val _currentAngle = MutableStateFlow(
        value = AngleMeasurements.Measurement(0F, -1L)
    )
    val currentAngle: StateFlow<AngleMeasurements.Measurement> = _currentAngle

    private val _internalUiState = MutableStateFlow(InternalDataUiState())
    val internalUiState: StateFlow<InternalDataUiState> = _internalUiState

    private var streamType: StreamType? = null

    private val _offsets = MutableStateFlow<List<Offset>>(emptyList())
    val offsets: StateFlow<List<Offset>> = _offsets

    fun stopDataStream() {
        when (streamType) {
            StreamType.SINGLE -> {
                internalSensorController.stopImuStream()
            }

            StreamType.DUAL -> {
                internalSensorController.stopImuStream()
                internalSensorController.stopGyroStream()
            }

            else -> {}
        }
        _internalUiState.update { it.copy(measuring = false) }
        Log.d(TAG, "Stream stopped.")
        Log.d(TAG, "No of measurements saved: " + measurements.size)
        stopCounter()
        _internalUiState.update {
            it.copy(
                showSaveButton = true,
                countDownTimer = _internalUiState.value.selectedTimerValue.toInt()
            )
        }

    }

    fun startMeasurement() {
        measurements.clear()
        _internalUiState.update { it.copy(startTime = -1L, showSaveButton = false) }
        if (_internalUiState.value.dualMeasurement) {
            Log.d(TAG, "DUAL")
            startAccAndGyro()
        } else {
            Log.d(TAG, "SINGLE")
            startAcc()
        }
    }


    private fun startAccAndGyro() {
        viewModelScope.launch {
            internalSensorController.startDualStream()
            streamType = StreamType.DUAL
            _internalUiState.update { it.copy(measuring = true) }
        }
    }

    private fun startAcc() {
        viewModelScope.launch {
            internalSensorController.startImuStream()
            streamType = StreamType.SINGLE
            _internalUiState.update { it.copy(measuring = true) }
        }

    }


    fun exportData() {
        csvExporter.exportMeasurements(measurements)
    }


    private fun startCounter() {
        if (timer == null) {
            _internalUiState.update { it.copy(timeInMs = 0) }
            timer = viewModelScope.launch {
                var counter = 0
                while (_internalUiState.value.countDownTimer > 0) {
                    delay(TimerValues.UPDATE_TIME)
                    _internalUiState.update { it.copy(timeInMs = _internalUiState.value.timeInMs + TimerValues.UPDATE_TIME) }
                    if (_internalUiState.value.selectedTimerValue < TimerValues.MAX_TIMER) {
                        counter++
                        if (counter == 10) {
                            _internalUiState.update { it.copy(countDownTimer = _internalUiState.value.countDownTimer - 1) }
                            counter = 0
                        }
                    }

                }
                stopDataStream()
                //Log.d(TAG, "Last x: " + _offsets.value.last().x)
            }
        }
    }

    private fun stopCounter() {
        if (timer != null) {
            timer!!.cancel()
            timer = null
        }
    }

    private fun addToOffsets(measurement: AngleMeasurements.Measurement) {
        if (timer == null) {
            startCounter()
            _internalUiState.update { it.copy(streamStarted = Helpers.getFormattedDateTimeNow()) }
            Log.d(TAG, "Counting up")
        }

        val yValue = Canvas.convertAngleToY(
            _internalUiState.value.canvasHeight,
            measurement.angle
        ) // Canvas is 1000, start at 500 (middle), multiply angle to fill space
        val xValue = Canvas.convertTimestampToX(
            measurement.timestamp,
            _internalUiState.value.selectedTimerValue,
            _internalUiState.value.startTime
        )
        if (xValue >= _internalUiState.value.canvasWidth && floor(_internalUiState.value.selectedTimerValue) > 30 || xValue < 0) {
            _offsets.value = emptyList()
            _internalUiState.update { it.copy(startTime = measurement.timestamp) }
        } else {
            _offsets.value = _offsets.value + Offset(xValue, yValue)
        }
    }

    fun setSingleMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = false, startTime = -1) }
    }

    fun setDualMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = true, startTime = -1) }
    }

    fun setTimerValue(value: Float) {
        _internalUiState.update {
            it.copy(
                selectedTimerValue = value,
                countDownTimer = value.toInt()
            )
        }
    }

    fun setCanvasDimension(canvasWidth: Float, canvasHeight: Float) {
        _internalUiState.update { it.copy(canvasWidth = canvasWidth, canvasHeight = canvasHeight) }
    }

    fun saveToDb() {
        val timeMeasured = _internalUiState.value.timeInMs
        if (timeMeasured <= 0.5) {
            return
        }
        viewModelScope.launch {
            val dateTime: LocalDateTime = LocalDateTime.parse(_internalUiState.value.streamStarted)
            val measurementType: MeasurementType = MeasurementType.INTERNAL
            measurementsRepository.insertMeasurements(
                measurements = measurements,
                type = measurementType,
                dateTime = dateTime,
                timeMeasured = timeMeasured
            )
            Log.d(TAG, measurementsRepository.getSize().toString())

            _internalUiState.update { it.copy(showSaveButton = false) }

        }
    }

    private enum class StreamType {
        SINGLE, DUAL
    }

    init {
        viewModelScope.launch {
            internalSensorController.angleMeasurementCurrent.distinctUntilChangedBy {
                it?.timestamp
            }
                .collect {
                    if (it != null) {
                        _currentAngle.value = it
                        addToOffsets(it)
                        measurements.add(it)
                    }
                }
        }
    }

}

data class InternalDataUiState(
    val measuring: Boolean = false,
    val dualMeasurement: Boolean = false,
    val selectedTimerValue: Float = TimerValues.MIN_TIMER.toFloat(),
    val countDownTimer: Int = TimerValues.MIN_TIMER,
    val startTime: Long = -1L,
    val canvasWidth: Float = 1000F,
    val canvasHeight: Float = 1000F,
    val showSaveButton: Boolean = false,
    val streamStarted: String = "",
    val timeInMs: Long = 0L
)