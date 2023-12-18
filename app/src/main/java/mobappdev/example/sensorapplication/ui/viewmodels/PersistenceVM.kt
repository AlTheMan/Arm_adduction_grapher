package mobappdev.example.sensorapplication.ui.viewmodels

import android.util.Log
import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.tabs.TabLayout.TabGravity
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.persistence.MeasurementsEntity
import mobappdev.example.sensorapplication.persistence.MeasurementsRepository
import mobappdev.example.sensorapplication.persistence.dto.MeasurementSummary
import mobappdev.example.sensorapplication.ui.shared.Canvas
import javax.inject.Inject

@HiltViewModel
class PersistenceVM @Inject constructor(
    private val measurementsRepository: MeasurementsRepository
) : ViewModel() {

    private val _uiState = MutableStateFlow<UiState>(UiState())

    private val _measurementsSummary = MutableStateFlow<List<MeasurementSummary>>(emptyList())
    val measurementSummary: StateFlow<List<MeasurementSummary>> = _measurementsSummary

    private val _offsets = MutableStateFlow<List<Offset>>(emptyList())
    val offsets: StateFlow<List<Offset>> = _offsets


    fun deleteMeasurement(id: Int) {
        viewModelScope.launch {
            measurementsRepository.delete(id)
        }
    }

    fun getMeasurement(id: Int){
        _offsets.value = emptyList()
        viewModelScope.launch {
            val measurement = measurementsRepository.get(id)
            calculateOffsets(measurement)
        }
    }

    private fun calculateOffsets(measurement: MeasurementsEntity) {
        var startTime: Long = -1
        Log.d("ASD", measurement.timeMeasured.toString())
        for (m in measurement.measurements) {
            if (startTime < 0) {
                startTime = m.timestamp
            }

            val x = Canvas.convertTimestampToX(timestamp = m.timestamp, timerValue = (measurement.timeMeasured / 1000.0).toFloat(), startTime = startTime)
            val y = Canvas.convertAngleToY(canvasHeight = _uiState.value.canvasHeight, angle = m.angle)
            Log.d("ASD", "$x $y")
            _offsets.value = _offsets.value + Offset(x, y)
        }
    }

    fun setDimensions(canvasWidth: Float, canvasHeight: Float) {
        _uiState.update { it.copy(canvasWidth = canvasWidth, canvasHeight = canvasHeight) }

    }

    init {
        viewModelScope.launch {
            measurementsRepository.getAllSummary().collect {
                val newList = it
                _measurementsSummary.value = newList
            }
        }
    }




}

data class UiState (
    val canvasWidth: Float = 1000F,
    val canvasHeight: Float = 1000F,
)