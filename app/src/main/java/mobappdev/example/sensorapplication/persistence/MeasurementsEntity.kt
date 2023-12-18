package mobappdev.example.sensorapplication.persistence

import androidx.room.Entity
import androidx.room.PrimaryKey
import mobappdev.example.sensorapplication.persistence.dto.MeasurementDTO
import java.time.LocalDateTime

@Entity(tableName = "measurements")
data class MeasurementsEntity(
    @PrimaryKey(autoGenerate = true)
    var id: Int? = null,
    var type: MeasurementType,
    var timeOfMeasurement: LocalDateTime,
    var measurements: List<MeasurementDTO>
)
