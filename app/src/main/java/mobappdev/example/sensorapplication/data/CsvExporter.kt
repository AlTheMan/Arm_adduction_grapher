package mobappdev.example.sensorapplication.data

import android.content.ContentValues
import android.content.Context
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import androidx.annotation.RequiresApi
import java.io.File
import mobappdev.example.sensorapplication.persistence.converters.MeasurementConverters
import mobappdev.example.sensorapplication.persistence.dto.MeasurementDTO
import java.io.FileOutputStream
import java.io.IOException
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.math.pow

private const val TAG = "CSV"


class CsvExporter(private val context: Context) {
    //val csvExporter = CsvExporter(this) // 'this' refers to an Activity or Service context

    @RequiresApi(Build.VERSION_CODES.O)
    fun exportDataTest() {
        val fileName = "AnalysisData.csv"
        val dataBuilder = StringBuilder()

        val date = Date()  // Or set specific Date
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)

        val data = arrayOf("Ship Name", "Scientist Name", "...", formattedDate)
        dataBuilder.append(data.joinToString(",")).append("\n")

        Log.d(TAG, "inside exportData2")
        saveToDownloadsFolder(context, fileName, dataBuilder.toString())
    }


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveToDownloadsFolder(context: Context, fileName: String, data: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) { //MediaStore API for API levels >=29
            Log.d(TAG, "running android version 11+, API level >=29")
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
                        Log.d(TAG, "successfully saved file: " + fileName)
                        Log.d(TAG, "file saved at path: " + uri)

                    }
                } ?: throw IOException("Failed to create new MediaStore record.")
            } catch (e: IOException) {
                Log.d("CSV", "Failed to saved file: " + e)
            }
        }else{ //for android API version <29
            Log.d(TAG, "running older version of android")
            //val file3 = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOCUMENTS), fileName)
            val file = File(Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS), fileName)
            //val file2 = File(context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS), fileName)
            try {
                FileOutputStream(file).use { outputStream ->
                    outputStream.write(data.toByteArray())
                    Log.d(TAG, "Successfully saved file: $fileName")
                    Log.d(TAG, "File saved at: " + file.absolutePath)
                }
            } catch (e: IOException) {
                Log.d(TAG, "Failed to save file: ${e.message}")
            }
        }
    }


    /**
     * Angle: limits the angle to two decimals and..
     * Timestamp: zeros the first number and reduces all consequent numbers by the same ammount. Counts in miliseconds innstead of nanoseconds
     */
    private fun truncateMeasurements(measurements: List<AngleMeasurements.Measurement>):List<AngleMeasurements.Measurement>{
        if(measurements.isEmpty()) return measurements

        val initialTimestamp:Long=measurements.first().timestamp
        val truncatedMeasurements: MutableList<MeasurementDTO> = mutableListOf()
        for(measurement in measurements){
            val normalizedAngle = String.format(Locale.US,"%.2f", measurement.angle).toFloat() //limits the angle to two decimals
            val normalizedTimestamp = ((measurement.timestamp - initialTimestamp) * 10.0.pow(-6.0)).toLong() //zeros the first number. counts in miliseconds innstead of nanoseconds
            truncatedMeasurements.add(MeasurementDTO(normalizedAngle, normalizedTimestamp))
        }
        return truncatedMeasurements
    }

    /**
     * exports in csv format. Angle is in degrees (C) and timestamp in miliseconds.
     * Exports to download-folder in android phone
     */

    fun exportMeasurements(measurements: List<AngleMeasurements.Measurement>){
        var truncatedMeasurements = truncateMeasurements(measurements)
        val dtoMeasurements = MeasurementConverters.toDto(truncatedMeasurements)
        exportMeasurements2(dtoMeasurements)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun exportMeasurements2(measurements: List<MeasurementDTO>) {
        val fileName = "adduction.csv"
        val dataBuilder = StringBuilder()

        //CAdding SV date header
        val date = Date()
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault())
        val formattedDate = dateFormat.format(date)
        var headerDate = arrayOf("Date: ", formattedDate.toString())
        dataBuilder.append(headerDate.joinToString(",")).append("\n")

        // Adding CSV headers
        val headers = arrayOf("Angle (°C)", "Timestamp (ms)")
        dataBuilder.append(headers.joinToString(",")).append("\n")

        // Iterating through the measurements and appending each as a CSV row
        for (measurement in measurements) {
            //val formattedTimestamp = SimpleDateFormat("yyyy-MM-dd HH:mm:ss", Locale.getDefault()).format(Date(measurement.timestamp))
            val row = arrayOf(measurement.angle.toString(), measurement.timestamp.toString())
            dataBuilder.append(row.joinToString(",")).append("\n")
        }

        Log.d(TAG, "inside exportMeasurements")
        saveToDownloadsFolder(context, fileName, dataBuilder.toString())
    }

}
