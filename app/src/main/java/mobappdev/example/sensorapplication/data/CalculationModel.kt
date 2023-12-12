package mobappdev.example.sensorapplication.data

import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "CalculationModel"
private const val alpha = 0.95F //TODO: Ändra från ui?

class CalculationModel {

    private var angleMeasurements: MutableList<Float> = mutableListOf()

    fun getLinearAccelerationAngle(axes: Triple<Float, Float, Float>): Float {
        val pitch = getPitchAngle(axes)
        return radiansToDegrees(linearAccelerationFilter(pitch))
    }

    /** Rotate x-direction */
    private fun getPitchAngle(axes: Triple<Float, Float, Float>): Float {
        return atan(axes.first / distance(axes.second, axes.third))
    }

    /** Rotate y-direction */
    private fun getRollAngle(axes: Triple<Float, Float, Float>): Float {
        return atan(axes.second / distance(axes.first, axes.third))
    }

    private fun radiansToDegrees(rad: Float): Float {
        return rad * (180 / Math.PI).toFloat()
    }

    private fun sensorFusionFilter(linAccAngle: Float, gyroAngle: Float): Float {
        return alpha * linAccAngle + (1 - alpha) * gyroAngle
    }

    private fun linearAccelerationFilter(value: Float): Float {
        if (angleMeasurements.isEmpty()) {
            angleMeasurements.add(value)
            return value
        }
        return (alpha * value) + (1 - alpha) * angleMeasurements.last()
    }

    private fun distance(a: Float, b: Float): Float {
        return sqrt(a.pow(2) + b.pow(2))
    }


}