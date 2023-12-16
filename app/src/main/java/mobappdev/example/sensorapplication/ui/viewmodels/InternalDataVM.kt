package mobappdev.example.sensorapplication.ui.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.data.AngleMeasurements
import mobappdev.example.sensorapplication.domain.InternalSensorController
import mobappdev.example.sensorapplication.ui.shared.Canvas
import mobappdev.example.sensorapplication.ui.shared.TimerValues
import javax.inject.Inject

private const val MAX_TIMER = 31
private const val TAG = "InternalDataVM"

@HiltViewModel
class InternalDataVM @Inject constructor(
    private val internalSensorController: InternalSensorController
) : ViewModel() {

    private var countDownTimer: CountDownTimer? = null

   // val angleCurrentInternal = internalSensorController.angleMeasurementCurrent

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
    }

    fun startMeasurement() {
        _internalUiState.update { it.copy(startTime = -1L) }
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

    private fun startCountdownTimer(totalTime: Long) {
        viewModelScope.launch {
            countDownTimer = object : CountDownTimer(totalTime, TimerValues.COUNTDOWN_INTERVAL.toLong()) {
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
        val yValue = Canvas.convertAngleToY(_internalUiState.value.canvasHeight, measurement.angle) // Canvas is 1000, start at 500 (middle), multiply angle to fill space
        val xValue = Canvas.convertTimestampToX(measurement.timestamp, _internalUiState.value.selectedTimerValue, _internalUiState.value.startTime)
        if (xValue >= _internalUiState.value.canvasWidth || xValue < 0) {
            _offsets.value = emptyList()
            _internalUiState.update { it.copy(startTime = measurement.timestamp) }
        } else {
            _offsets.value = _offsets.value + Offset(xValue, yValue)
        }
        Log.d(TAG, "List size: " + _offsets.value.size.toString() + "| X: " + String.format("%.1f",xValue) + "| Y: " + yValue)
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
    val canvasHeight: Float = 1000F
)