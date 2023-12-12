package mobappdev.example.sensorapplication.data

import android.util.Log
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "CalculationModel"

object CalculationModel {

    fun getLinAccAngle(measurement: Triple<Float, Float, Float>) : Float {
        var pitch = atan(measurement.first / distance(measurement.second, measurement.third))
        //var roll = atan(measurement.second / distance(measurement.first, measurement.third))
        pitch = radiansToDegrees(pitch)
        //roll = radiansToDegrees(roll)
        //Log.d(TAG, pitch.toString())
        //Log.d(TAG, roll.toString())
        return pitch
    }
    private fun distance (a: Float, b: Float): Float {
        return sqrt(a.pow(2) + b.pow(2))
    }

    private fun radiansToDegrees(rad: Float): Float {
        return rad * (180 / Math.PI).toFloat()
    }









}