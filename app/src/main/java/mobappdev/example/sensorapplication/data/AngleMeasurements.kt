package mobappdev.example.sensorapplication.data

data class AngleMeasurements(
    var list: MutableList<measurment> = mutableListOf()
){
    data class measurment(
        val angle: Float = 0f,
        val timestamp: Long =0
    )
}