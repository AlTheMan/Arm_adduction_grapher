package mobappdev.example.sensorapplication.ui.viewmodels

import androidx.compose.ui.geometry.Offset
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.persistence.MeasurementsRepository
import mobappdev.example.sensorapplication.persistence.dto.MeasurementSummary
import javax.inject.Inject

@HiltViewModel
class PersistenceVM @Inject constructor(
    private val measurementsRepository: MeasurementsRepository
) : ViewModel() {



    private val _measurementsSummary = MutableStateFlow<List<MeasurementSummary>>(emptyList())
    val measurementSummary: StateFlow<List<MeasurementSummary>> = _measurementsSummary

    private val _offsets = MutableStateFlow<List<Offset>>(emptyList())
    val offsets: StateFlow<List<Offset>> = _offsets


    fun deleteMeasurement(id: Int) {
        viewModelScope.launch {
            measurementsRepository.delete(id)
        }
    }


    fun setCanvas(id: Int){
        viewModelScope.launch {
            val measurement = measurementsRepository.get(id)
        }
    }

    fun setDimensions(canvasWidth: Float, canvasHeight: Float) {


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