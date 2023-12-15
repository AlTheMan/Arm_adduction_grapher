package mobappdev.example.sensorapplication.ui.components

import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable

@Composable
fun NumberPickerSlider(
    range: ClosedRange<Int>, selectedNumber: Float, onNumberSelected: (Float) -> Unit
) {
    Slider(
        value = if (selectedNumber <= 30) selectedNumber else 100f,
        onValueChange = onNumberSelected,
        valueRange = range.start.toFloat()..range.endInclusive.toFloat(),
        steps = range.endInclusive - range.start - 1
    )
}