package mobappdev.example.sensorapplication.data

import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt

private const val TAG = "CalculationModel"
private const val alpha = 0.95F //TODO: Ändra från ui?


class CalculationModel {

    //private var angleMeasurements2: MutableList<Float> = mutableListOf()
    //private var angleMeasurements: MutableList<Pair<Float, Long>> = mutableListOf()
    //private var angleMeasurments = AngleMeasurements()
    private var angle: Float =0f
    private var addedMeasurement: Boolean = false

    //private val _angleMeasurementsFlow = MutableStateFlow(angleMeasurments)
    //val angleMeasurementsFlow: StateFlow<AngleMeasurements>
    //    get() = _angleMeasurementsFlow.asStateFlow()

    //private val _angleMeasurementLastFlow= MutableStateFlow(angleMeasurments.list.last())
    //val angleMeasurementLastFlow: StateFlow<AngleMeasurements.measurment>
     //   get() = _angleMeasurementLastFlow.asStateFlow()

    //private val _angleMeasurementLastFlow = MutableStateFlow(
    //    _angleMeasurementsFlow.value.list.lastOrNull() ?: AngleMeasurements.measurment(0f, 0L)
    //)
    //val angleMeasurementLastFlow: StateFlow<AngleMeasurements.measurment>
    //    get() = _angleMeasurementLastFlow.asStateFlow()

    fun reset(){
        addedMeasurement=false;
        angle=0f
    }

    fun getLinearAccelerationAngle(axes: Triple<Float, Float, Float>, timestamp: Long): AngleMeasurements.measurment {
        val pitch = getPitchAngle(axes)
        val angleInDegrees = radiansToDegrees(linearAccelerationFilter(pitch))
        return AngleMeasurements.measurment(angleInDegrees, timestamp)
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
        if (!addedMeasurement) {
            addedMeasurement=true;
            return value
        }
        val lastAngle = angle
        val filteredValue = alpha * value + (1 - alpha) * lastAngle
        angle=filteredValue
        return filteredValue
    }

    private fun distance(a: Float, b: Float): Float {
        return sqrt(a.pow(2) + b.pow(2))
    }


}