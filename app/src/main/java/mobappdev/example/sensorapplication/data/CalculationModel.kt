package mobappdev.example.sensorapplication.data

import android.util.Log
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "CalculationModel"
private const val linearAlpha = 0.9F //TODO: Ändra från ui?
private const val gyroAlpha = 0.9F


class CalculationModel {

    private var angleAcc: Float = 0f
    private var angleGyro: Float = 0f
    private var addedMeasurementAcc: Boolean = false
    private var addedMeasurementGyro: Boolean = false

    private var lastGyroTimestamp: Long = -1
    private var cumulativeGyroAngleX: Float = 0f
    private var cumulativeGyroAngleY: Float = 0f
    private var cumulativeGyroAngleZ: Float = 0f

    fun reset() {
        addedMeasurementAcc = false
        addedMeasurementGyro = false
        angleAcc = 0f
        angleGyro = 0f
        lastGyroTimestamp = -1
        cumulativeGyroAngleX = 0f
        cumulativeGyroAngleY = 0f
        cumulativeGyroAngleZ = 0f
    }

    fun getLinearAccelerationAngleWithGyroFilter(
        axes: Triple<Float, Float, Float>,
        gyroscope: Triple<Float, Float, Float>,
        timestamp: Long
    ): Float {
        //val accFiltered = getLinearAccelerationAngle(axes)
        val accAsDegrees = radiansToDegrees(getPitchAngle(axes))
        val gyroAngles = getGyroAngles(gyroscope, timestamp)
        return sensorFusionFilter(accAsDegrees, gyroAngles.first)
    }

    private fun getGyroAngles(gyroscope: Triple<Float, Float, Float>, timeStamp: Long) : Triple<Float, Float, Float> {
        if (lastGyroTimestamp < 0) {
            lastGyroTimestamp = timeStamp
            return Triple(0F,0F,0F)
        }
        Log.d(TAG, lastGyroTimestamp.toString())

        val deltaTime = (timeStamp - lastGyroTimestamp) / 1_000_000_000.0

        Log.d(TAG, "Delta time: $deltaTime")

        lastGyroTimestamp = timeStamp

        val deltaAngleX = radiansToDegrees(gyroscope.first * deltaTime.toFloat())
        val deltaAngleY = radiansToDegrees(gyroscope.second * deltaTime.toFloat())
        val deltaAngleZ = radiansToDegrees(gyroscope.third * deltaTime.toFloat())

        Log.d(TAG, "Delta angle: $deltaAngleZ")


        cumulativeGyroAngleX += deltaAngleX
        cumulativeGyroAngleY += deltaAngleY
        cumulativeGyroAngleZ += deltaAngleZ

        Log.d(TAG, "Cum angle: $cumulativeGyroAngleZ")



        return Triple(cumulativeGyroAngleX, cumulativeGyroAngleY, cumulativeGyroAngleZ)
    }

    fun getLinearAccelerationAngle(axes: Triple<Float, Float, Float>): Float {
        val pitch = getPitchAngle(axes)
        return radiansToDegrees(linearAccelerationFilterAcc(pitch))
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

    private fun nonNegativeAngle(defaultAngle: Float): Float {
        var nonNegativeAngle = defaultAngle
        if (defaultAngle < 0) nonNegativeAngle = defaultAngle * -1
        return nonNegativeAngle
    }

    private fun sensorFusionFilter(linAccAngle: Float, gyroAngle: Float): Float {
        return gyroAlpha * linAccAngle + (1 - gyroAlpha) * gyroAngle
    }

    private fun linearAccelerationFilterGyro(value: Float): Float {
        if (!addedMeasurementGyro) {
            addedMeasurementGyro = true
            return value
        }
        val filteredValue = linearAlpha * value + (1 - linearAlpha) * angleGyro
        angleGyro = filteredValue
        return filteredValue
    }

    private fun linearAccelerationFilterAcc(value: Float): Float {
        if (!addedMeasurementAcc) {
            addedMeasurementAcc = true
            return value
        }
        val lastAngle = angleAcc
        val filteredValue = linearAlpha * value + (1 - linearAlpha) * lastAngle
        angleAcc = filteredValue
        return filteredValue
    }

    private fun distance(a: Float, b: Float): Float {
        return sqrt(a.pow(2) + b.pow(2))
    }


}