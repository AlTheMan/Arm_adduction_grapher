package mobappdev.example.sensorapplication.data

import java.io.File
import java.io.FileWriter
import com.opencsv.CSVWriter
import java.io.IOException
import java.io.OutputStream
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class CsvExporter {

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
