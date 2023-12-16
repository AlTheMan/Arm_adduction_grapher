package mobappdev.example.sensorapplication.ui.shared

import kotlinx.coroutines.flow.update
import kotlin.math.ceil

object Canvas {

    fun convertTimestampToX(timestamp: Long, timerValue: Float, startTime: Long): Float {
        if (startTime < 0) {
            return -1F
        }
        val divider = (1000000 * timerValue)
        return ceil((timestamp - startTime).toFloat() / divider)
    }

    fun convertAngleToY(canvasHeight: Float, angle: Float) : Float {
        return canvasHeight / 2 + angle * 3
    }





   /* private fun convertTimestampToX(timestamp: Long): Float {
        val divider = (1000000 * _internalUiState.value.selectedTimerValue)
        if (_internalUiState.value.startTime < 0) {
            _internalUiState.update { it.copy(startTime = timestamp) }
            //Log.d(TAG, "First timetamp is: " + timestamp + "," + _internalUiState.value.startTime)
        }

        val xVal = (timestamp - _internalUiState.value.startTime).toFloat() / divider
        if (xVal > _internalUiState.value.canvasWidth) { // resets the canvas if Inf timer
            _offsets.value = emptyList()
            _internalUiState.update { it.copy(startTime = timestamp) }

        }


        return ceil((timestamp - _internalUiState.value.startTime).toFloat() / divider)
    }*/


}