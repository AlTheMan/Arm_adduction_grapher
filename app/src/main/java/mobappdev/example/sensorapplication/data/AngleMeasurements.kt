package mobappdev.example.sensorapplication.data

data class AngleMeasurements(
    var list: MutableList<measurment> = mutableListOf()
){
    data class measurment(
        var angle: Float = 0f,
        var timestamp: Long =0
    )
}