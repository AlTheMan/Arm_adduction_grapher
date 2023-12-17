package mobappdev.example.sensorapplication.persistence

import mobappdev.example.sensorapplication.data.AngleMeasurements
import mobappdev.example.sensorapplication.persistence.converters.MeasurementConverters
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
        dateTime: LocalDateTime
    ) {
        measurementsDao.insert(
            MeasurementsEntity(
                type = type,
                timeOfMeasurement = dateTime,
                measurements = MeasurementConverters.toDto(measurements)
            )
        )
    }




}