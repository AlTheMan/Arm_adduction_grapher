package mobappdev.example.sensorapplication.ui.components

import androidx.compose.foundation.layout.Row
import androidx.compose.material3.Slider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable

@Composable
fun NumberPickerSlider(
    range: ClosedRange<Int>, selectedNumber: Float, onNumberSelected: (Float) -> Unit
) {
    Row {
        Text(text = "Timer: " + if (selectedNumber < 31) String.format("%.0fs", selectedNumber) else "Inf")
    }
    Slider(
        value = selectedNumber,
        onValueChange = onNumberSelected,
        valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
        steps = range.endInclusive - range.start - 1
    )
    

    
    
}