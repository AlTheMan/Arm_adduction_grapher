package mobappdev.example.sensorapplication.data

import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "CalculationModel"
private const val alpha = 0.95F //TODO: Ändra från ui?


class CalculationModel {

    //private var angleMeasurements2: MutableList<Float> = mutableListOf()
    //private var angleMeasurements: MutableList<Pair<Float, Long>> = mutableListOf()
    private var angleMeasurments = AngleMeasurements()


    fun getLinearAccelerationAngle(axes: Triple<Float, Float, Float>, timestamp: Long): AngleMeasurements.measurment {
        val pitch = getPitchAngle(axes)
        val angle = radiansToDegrees(linearAccelerationFilter(pitch, timestamp))
        return AngleMeasurements.measurment(angle, timestamp)

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

    private fun linearAccelerationFilter(value: Float, timestamp: Long): Float {
        if (angleMeasurments.list.isEmpty()) {
            angleMeasurments.list.add(AngleMeasurements.measurment(value, timestamp))
            return value
        }
        val lastAngle = angleMeasurments.list.last().angle
        val filteredValue = alpha * value + (1 - alpha) * lastAngle
        angleMeasurments.list.add(AngleMeasurements.measurment(filteredValue, timestamp))

        return filteredValue
    }

    private fun distance(a: Float, b: Float): Float {
        return sqrt(a.pow(2) + b.pow(2))
    }


}