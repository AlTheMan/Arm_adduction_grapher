package mobappdev.example.sensorapplication.data

import android.location.GnssMeasurement
import android.util.Log
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "CalculationModel"

object CalculationModel {

    fun getLinAccAngle(measurement: Triple<Float, Float, Float>) : Float {
        val norm = normVector(measurement)


        return atan(measurement.first / distance(measurement.second, measurement.third))
    }
    private fun distance (y: Float, z: Float): Float {
        val value =  sqrt(y.pow(2) + z.pow(2))
        Log.d(TAG, value.toString())
        return value
    }

    private fun normVector(m: Triple<Float, Float, Float>): Float {
        return (m.first.pow(2) + m.second.pow(2) + m.third.pow(3))
    }









}