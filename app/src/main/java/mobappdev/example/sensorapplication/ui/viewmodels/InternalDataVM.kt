package mobappdev.example.sensorapplication.ui.viewmodels

import android.os.CountDownTimer
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
import mobappdev.example.sensorapplication.domain.InternalSensorController
import mobappdev.example.sensorapplication.persistence.MeasurementType
import mobappdev.example.sensorapplication.persistence.MeasurementsRepository
import mobappdev.example.sensorapplication.ui.shared.Canvas
import mobappdev.example.sensorapplication.ui.shared.TimerValues
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject

private const val TAG = "InternalDataVM"

@HiltViewModel
class InternalDataVM @Inject constructor(
    private val internalSensorController: InternalSensorController,
    private val measurementsRepository: MeasurementsRepository
) : ViewModel() {

    private var countDownTimer: CountDownTimer? = null
    private var timer: Job? = null
    private var activeTimer: Boolean = false

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
        if (countDownTimer != null) {
            cancelTimer()
        }
        Log.d(TAG, "Stream stopped.")
        Log.d(TAG, "No of measurements saved: " + measurements.size)
        _internalUiState.update { it.copy(showSaveButton = true) }
        stopCounter()
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


    private fun startCounter() {
        if (timer == null) {
            _internalUiState.update { it.copy(timeInMs = 0) }
            activeTimer = true
            timer = viewModelScope.launch {
                while (activeTimer) {
                    delay(1)
                    _internalUiState.update { it.copy(timeInMs = _internalUiState.value.timeInMs + 1) }
                }
            }
        }
    }

    private fun stopCounter(){
        if (timer != null) {
            activeTimer = false
            timer = null
            Log.d(TAG, "Timer: " + (_internalUiState.value.timeInMs / 1000.0)) // why isnt this showing any decimals?
        }
    }

    private fun startCountdownTimer(totalTime: Long) {
        viewModelScope.launch {
            countDownTimer =
                object : CountDownTimer(totalTime, TimerValues.COUNTDOWN_INTERVAL.toLong()) {
                    override fun onTick(millisUntilFinished: Long) {
                        println("Seconds remaining: ${millisUntilFinished / TimerValues.COUNTDOWN_INTERVAL}")
                        _internalUiState.update { it.copy(countDownTimer = (millisUntilFinished / TimerValues.COUNTDOWN_INTERVAL).toInt()) }
                    }

                    override fun onFinish() {
                        stopDataStream()
                        println("Timer finished")
                    }
                }
            countDownTimer?.start()
        }

    }

    private fun addToOffsets(measurement: AngleMeasurements.Measurement) {
        if (countDownTimer == null && _internalUiState.value.selectedTimerValue < TimerValues.MAX_TIMER) {
            startCountdownTimer(_internalUiState.value.selectedTimerValue.toLong() * TimerValues.COUNTDOWN_INTERVAL)
        }
        if (timer == null) {
            startCounter()
            Log.d(TAG, "Counting up")
        }
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        val formattedDate = now.format(formatter)
        _internalUiState.update { it.copy(streamStarted = formattedDate) }

        val yValue = Canvas.convertAngleToY(
            _internalUiState.value.canvasHeight,
            measurement.angle
        ) // Canvas is 1000, start at 500 (middle), multiply angle to fill space
        val xValue = Canvas.convertTimestampToX(
            measurement.timestamp,
            _internalUiState.value.selectedTimerValue,
            _internalUiState.value.startTime
        )
        if (xValue >= _internalUiState.value.canvasWidth || xValue < 0) {
            _offsets.value = emptyList()
            _internalUiState.update { it.copy(startTime = measurement.timestamp) }
        } else {
            _offsets.value = _offsets.value + Offset(xValue, yValue)
        }
    }


    private fun cancelTimer() {
        countDownTimer?.cancel()
        countDownTimer = null
    }

    fun setSingleMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = false) }
    }

    fun setDualMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = true) }
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