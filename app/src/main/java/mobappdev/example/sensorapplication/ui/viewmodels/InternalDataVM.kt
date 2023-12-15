package mobappdev.example.sensorapplication.ui.viewmodels

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.delay
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

    private val gyroDataFlow = internalSensorController.currentGyroUI
    private val linAccDataFlow = internalSensorController.currentLinAccUI
    val angleCurrentInternal = internalSensorController.angleMeasurementCurrent

    private val _internalUiState = MutableStateFlow(InternalDataUiState())
    val internalUiState: StateFlow<InternalDataUiState> = _internalUiState

    private var streamType: StreamType? = null


    fun stopDataStream(){
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
    }


    fun startAccAndGyro() {
        internalSensorController.startGyroStream()
        internalSensorController.startImuStream()
        streamType = StreamType.DUAL
        _internalUiState.update { it.copy(measuring = true) }
    }

    fun startAcc() {
        internalSensorController.startImuStream()
        streamType = StreamType.SINGLE
        _internalUiState.update { it.copy(measuring = true) }
        if (_internalUiState.value.selectedNumber < MAX_TIMER) {
            viewModelScope.launch {
                delay((_internalUiState.value.selectedNumber * 1000).toLong())
                stopDataStream()
            }
        }
    }

    fun setSingleMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = false) }
    }

    fun setDualMeasurement() {
        _internalUiState.update { it.copy(dualMeasurement = true) }
    }

    fun setTimerValue(value: Float) {
        _internalUiState.update { it.copy(selectedNumber = value) }
    }

    private enum class StreamType {
        SINGLE, DUAL
    }

}

data class InternalDataUiState(
    val measuring: Boolean = false,
    val dualMeasurement: Boolean = false,
    val selectedNumber: Float = 10f
)