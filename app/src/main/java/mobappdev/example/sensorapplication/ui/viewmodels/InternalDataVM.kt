package mobappdev.example.sensorapplication.ui.viewmodels

import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import mobappdev.example.sensorapplication.domain.InternalSensorController
import javax.inject.Inject

@HiltViewModel
class InternalDataVM @Inject constructor(
    private val internalSensorController: InternalSensorController
) {

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
    }

    private enum class StreamType {
        SINGLE, DUAL
    }
}

data class InternalDataUiState(
    val measuring: Boolean = false,
    val dualMeasurement: Boolean = false,
)