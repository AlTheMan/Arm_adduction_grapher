package mobappdev.example.sensorapplication.data

data class AngleMeasurements(
    var list: MutableList<measurment> = mutableListOf()
){
    data class measurment(
        val angle: Float,
        val timestamp: Long
    )
}