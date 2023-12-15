package mobappdev.example.sensorapplication.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import androidx.annotation.RequiresApi
import java.io.File
import java.io.FileWriter
import com.opencsv.CSVWriter
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExporter {

    @RequiresApi(Build.VERSION_CODES.Q)
    fun saveToDownloadsFolder(context: Context, fileName: String, data: String) {
        val values = ContentValues().apply {
            put(MediaStore.MediaColumns.DISPLAY_NAME, fileName)
            put(MediaStore.MediaColumns.MIME_TYPE, "text/csv")
            put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
        }

        val resolver = context.contentResolver
        val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
        try {
            uri?.let {
                resolver.openOutputStream(it).use { outputStream ->
                    outputStream?.write(data.toByteArray())
                    // Notify the user that the file was saved successfully
                }
            } ?: throw IOException("Failed to create new MediaStore record.")
        } catch (e: IOException) {
            // Handle the exception
        }
    }


    fun exportMeasurements(measurements: List<AngleMeasurements.Measurement>) {
        val baseDir = android.os.Environment.getExternalStorageDirectory().absolutePath
        val fileName = "MeasurementsData.csv"
        val filePath = "$baseDir${File.separator}$fileName"
        val file = File(filePath)

        val writer = if (file.exists() && !file.isDirectory) {
            CSVWriter(FileWriter(filePath, true))
        } else {
            CSVWriter(FileWriter(filePath))
        }

        // Writing headers
        val headers = arrayOf("Angle", "Timestamp")
        writer.writeNext(headers)

        // Writing data
        for (measurement in measurements) {
            val data = arrayOf(
                measurement.angle.toString(),
                SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(measurement.timestamp)
            )
            writer.writeNext(data)
        }

        writer.close()
    }


    fun exportData() {
        val baseDir = android.os.Environment.getExternalStorageDirectory().absolutePath
        val fileName = "AnalysisData.csv"
        val filePath = "$baseDir${File.separator}$fileName"
        val file = File(filePath)

        val writer = if (file.exists() && !file.isDirectory) {
            CSVWriter(FileWriter(filePath, true))
        } else {
            CSVWriter(FileWriter(filePath))
        }

        val date = Date()  // Or set specific Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        val data = arrayOf("Ship Name", "Scientist Name", "...", formattedDate)

        writer.writeNext(data)

        writer.close()
    }




 /*
    fun OutputStream.writeCsv(movies: List<Movie>) {
        val writer = bufferedWriter()
        writer.write(""""Year", "Score", "Title"""")
        writer.newLine()
        movies.forEach {
            writer.write("${it.year}, ${it.score}, \"${it.title}\"")
            writer.newLine()
        }
        writer.flush()
    }
    FileOutputStream("filename.csv").apply { writeCsv(movies) }
 */
    /*
    @Throws(IOException::class)
    fun File.writeAsCSV(values: List<List<String>>) {
        val csv = values.joinToString("\n") { line -> line.joinToString(", ") }
        writeText(csv)
    }

    @Throws(IOException::class)
    fun File.readAsCSV(): List<List<String>> {
        val splitLines = mutableListOf<List<String>>()
        forEachLine {
            splitLines += it.split(", ")
        }
        return splitLines
    } */
}
