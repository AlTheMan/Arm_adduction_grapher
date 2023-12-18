package mobappdev.example.sensorapplication.ui.shared

import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

object Helpers {

    fun getFormattedDateTimeNow() : String {
        val now = LocalDateTime.now()
        val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd'T'HH:mm:ss")
        return now.format(formatter)
    }

}