package mobappdev.example.sensorapplication.persistence

import kotlinx.coroutines.flow.Flow
import mobappdev.example.sensorapplication.data.AngleMeasurements
import mobappdev.example.sensorapplication.persistence.converters.MeasurementConverters
import mobappdev.example.sensorapplication.persistence.dto.MeasurementDTO
import mobappdev.example.sensorapplication.persistence.dto.MeasurementSummary
import java.time.LocalDateTime
import javax.inject.Inject

class MeasurementsRepository @Inject constructor(
    private val measurementsDao: MeasurementsDao
) {


    suspend fun getSize() : Int {
        return measurementsDao.numberOfMeasurements()
    }
    suspend fun insertMeasurements(
        measurements: List<AngleMeasurements.Measurement>,
        type: MeasurementType,
        dateTime: LocalDateTime,
        timeMeasured: Long
    ) {
        measurementsDao.insert(
            MeasurementsEntity(
                type = type,
                timeOfMeasurement = dateTime,
                measurements = MeasurementConverters.toDto(measurements),
                timeMeasured = timeMeasured
            )
        )
    }

    fun getAllSummary() : Flow<List<MeasurementSummary>> {
        return measurementsDao.getAllSummary()
    }

    suspend fun delete(id: Int) {
        measurementsDao.deleteById(id)
    }

    suspend fun get(id: Int): MeasurementsEntity {
        return measurementsDao.getMeasurement(id)
    }

}