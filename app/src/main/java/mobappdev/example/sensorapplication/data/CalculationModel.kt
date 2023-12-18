package mobappdev.example.sensorapplication.data

import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "CalculationModel"
private const val alpha = 0.95F //TODO: Ändra från ui?


class CalculationModel {

    private var angleAcc: Float =0f
    private var angleGyro: Float =0f
    private var addedMeasurementAcc: Boolean = false
    private var addedMeasurementGyro: Boolean = false


    fun reset(){
        addedMeasurementAcc=false;
        addedMeasurementGyro=false;
        angleAcc=0f
        angleGyro=0f
    }

    fun getLinearAccelerationAngleWithGyroFilter(axes: Triple<Float, Float, Float>, gyroscope: Triple<Float, Float, Float>): Float{
        val accFiltered = getLinearAccelerationAngle(axes)
        val pitch = getPitchAngle(gyroscope)
        val gyroFiltered = radiansToDegrees(linearAccelerationFilterGyro(pitch))
        return sensorFusionFilter(accFiltered, gyroFiltered)
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
    private fun nonNegativeAngle(defaultAngle: Float):Float{
        var nonNegativeAngle = defaultAngle
        if(defaultAngle<0) nonNegativeAngle= defaultAngle*-1
        return nonNegativeAngle
    }

    private fun sensorFusionFilter(linAccAngle: Float, gyroAngle: Float): Float {
        return alpha * linAccAngle + (1 - alpha) * gyroAngle
    }

    private fun linearAccelerationFilterGyro(value: Float): Float {
        if(!addedMeasurementGyro){
            addedMeasurementGyro=true
            return value
        }
        val filteredValue = alpha * value + (1 - alpha) * angleGyro
        angleGyro=filteredValue
        return filteredValue
    }

    private fun linearAccelerationFilterAcc(value: Float): Float {
        if (!addedMeasurementAcc) {
            addedMeasurementAcc=true;
            return value
        }
        val lastAngle = angleAcc
        val filteredValue = alpha * value + (1 - alpha) * lastAngle
        angleAcc=filteredValue
        return filteredValue
    }

    private fun distance(a: Float, b: Float): Float {
        return sqrt(a.pow(2) + b.pow(2))
    }


}