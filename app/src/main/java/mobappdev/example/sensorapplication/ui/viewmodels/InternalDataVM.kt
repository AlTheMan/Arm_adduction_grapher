package mobappdev.example.sensorapplication.ui.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import mobappdev.example.sensorapplication.domain.InternalSensorController
import javax.inject.Inject

private const val MAX_TIMER = 31
private const val TAG = "InternalDataVM"

@HiltViewModel
class InternalDataVM @Inject constructor(
    private val internalSensorController: InternalSensorController
) : ViewModel() {

    private val gyroDataFlow = internalSensorController.currentGyroUI
    private val linAccDataFlow = internalSensorController.currentLinAccUI
    var countDownTimer: CountDownTimer? = null
    val angleCurrentInternal = internalSensorController.angleMeasurementCurrent

    private val _internalUiState = MutableStateFlow(InternalDataUiState())
    val internalUiState: StateFlow<InternalDataUiState> = _internalUiState

    private var streamType: StreamType? = null


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

    fun startMeasurement(){
        if (_internalUiState.value.dualMeasurement) {
            startAccAndGyro()
        } else {
            startAcc()
        }
        if (_internalUiState.value.selectedTimerValue < MAX_TIMER) {
            startCountdownTimer(_internalUiState.value.selectedTimerValue.toLong() * 1000, 1000)
        }
    }


    private fun startAccAndGyro() {
        internalSensorController.startGyroStream()
        internalSensorController.startImuStream()
        streamType = StreamType.DUAL
        _internalUiState.update { it.copy(measuring = true) }
    }

    private fun startAcc() {
        internalSensorController.startImuStream()
        streamType = StreamType.SINGLE
        _internalUiState.update { it.copy(measuring = true) }
    }

    private fun startCountdownTimer(totalTime: Long, interval: Long) {
        countDownTimer = object : CountDownTimer(totalTime, interval) {
            override fun onTick(millisUntilFinished: Long) {
                println("Seconds remaining: ${millisUntilFinished / 1000}")
                _internalUiState.update { it.copy(countDownTimer = (millisUntilFinished / 1000).toInt()) }
            }

            override fun onFinish() {
                stopDataStream()
                println("Timer finished")
            }
        }
        countDownTimer?.start()
    }

    private fun cancelTimer(){
        countDownTimer?.cancel()
        countDownTimer = null;
    }

    fun setSingleMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = false) }
    }

    fun setDualMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = true) }
    }

    fun setTimerValue(value: Float) {
        _internalUiState.update { it.copy(selectedTimerValue = value, countDownTimer = value.toInt()) }
    }

    private enum class StreamType {
        SINGLE, DUAL
    }

}

data class InternalDataUiState(
    val measuring: Boolean = false,
    val dualMeasurement: Boolean = false,
    val selectedTimerValue: Float = 10f,
    val countDownTimer: Int = 10
)