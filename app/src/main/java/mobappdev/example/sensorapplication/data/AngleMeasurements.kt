package mobappdev.example.sensorapplication.data

data class AngleMeasurements(
    var list: MutableList<Measurement> = mutableListOf()
){
    data class Measurement(
        var angle: Float = 0f,
        var timestamp: Long = -1
    )
}