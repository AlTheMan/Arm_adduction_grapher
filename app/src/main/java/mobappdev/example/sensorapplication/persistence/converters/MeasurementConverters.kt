package mobappdev.example.sensorapplication.persistence.converters

import androidx.room.TypeConverter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import mobappdev.example.sensorapplication.data.AngleMeasurements
import mobappdev.example.sensorapplication.persistence.dto.MeasurementDTO

object MeasurementConverters {
    private val gson = Gson()
    @TypeConverter
    fun fromString(value: String): List<MeasurementDTO> {
        val listType = object : TypeToken<List<MeasurementDTO>>() {}.type
        return gson.fromJson(value, listType)
    }

    @TypeConverter
    fun fromList(list: List<MeasurementDTO>): String {
        return gson.toJson(list)
    }


    fun toDto(measurements: List<AngleMeasurements.Measurement>) : List<MeasurementDTO> {
        val dto: MutableList<MeasurementDTO> = mutableListOf()
        for (m in measurements) {
            dto.add(MeasurementDTO(m.angle, m.timestamp))
        }
        return dto
    }


}