package mobappdev.example.sensorapplication.persistence.dto

import mobappdev.example.sensorapplication.persistence.MeasurementType
import java.time.LocalDateTime

data class MeasurementSummary(
    val id: Int,
    val type: MeasurementType,
    val timeOfMeasurement: LocalDateTime
)
