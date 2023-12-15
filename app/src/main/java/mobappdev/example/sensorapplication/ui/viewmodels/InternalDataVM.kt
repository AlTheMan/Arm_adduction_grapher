package mobappdev.example.sensorapplication.ui.viewmodels

import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.domain.InternalSensorController
import javax.inject.Inject

private const val MAX_TIMER = 31
private const val TAG = "InternalDataVM"

@HiltViewModel
class InternalDataVM @Inject constructor(
    private val internalSensorController: InternalSensorController
) : ViewModel() {

    private var countDownTimer: CountDownTimer? = null
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
            Log.d(TAG, "DUAL")
            startAccAndGyro()
        } else {
            Log.d(TAG, "SINGLE")
            startAcc()
        }
        if (_internalUiState.value.selectedTimerValue < MAX_TIMER) {
            Log.e(TAG, "Here3")
            startCountdownTimer(_internalUiState.value.selectedTimerValue.toLong() * 1000)
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
            countDownTimer = object : CountDownTimer(totalTime, 1000) {
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
            Log.e(TAG, "Here4")
        }

    }

    private fun cancelTimer(){
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