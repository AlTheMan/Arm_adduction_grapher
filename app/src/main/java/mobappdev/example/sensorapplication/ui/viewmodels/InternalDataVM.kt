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
import java.util.LinkedList
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

    private val _offsets = MutableStateFlow<List<Offset>>(emptyList())
    private var removeFromList: Boolean = false;
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

    fun startMeasurement(){
        removeFromList = false
        _internalUiState.update { it.copy(startTime = -1L) }
        _offsets.value = emptyList()
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
        }

    }
    private fun addToOffsets(measurement: AngleMeasurements.Measurement) {
        val yValue = 180 - measurement.angle
        val xValue = convertTimestampToX(measurement.timestamp)

        if (removeFromList) {
            val newList = LinkedList(_offsets.value) // Create a new instance of the list
            newList.add(Offset(xValue, yValue))
            if (newList.size > 1) {
                newList.removeFirst()
            }
            _offsets.value = newList
        } else {
            _offsets.value = _offsets.value + Offset(xValue, yValue) // this is
        }
        Log.d(TAG, "List size: " + _offsets.value.size.toString() + "| X: " + String.format("%.1f", xValue) + "| Y: " + yValue)
    }

    private fun convertTimestampToX(timestamp: Long) : Float {
        if (_internalUiState.value.startTime < 0){
            _internalUiState.update { it.copy(startTime = timestamp) }
            Log.d(TAG, "First timetamp is: " + timestamp  + "," + _internalUiState.value.startTime)
        }

        val xVal = (timestamp - _internalUiState.value.startTime).toFloat() / 10000000
        if (xVal > 1000) {
            removeFromList = true;
            _internalUiState.update { it.copy(startTime = timestamp) }

        }


        return (timestamp - _internalUiState.value.startTime).toFloat() / 10000000
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

    init {
        viewModelScope.launch {
            internalSensorController.angleMeasurementCurrent.distinctUntilChangedBy {
                it?.timestamp
            }
                .collect {
                if (it != null) {
                   addToOffsets(it)
                }
            }
        }
    }

}

data class InternalDataUiState(
    val measuring: Boolean = false,
    val dualMeasurement: Boolean = false,
    val selectedTimerValue: Float = 10f,
    val countDownTimer: Int = 10,
    val startTime: Long = -1L
)