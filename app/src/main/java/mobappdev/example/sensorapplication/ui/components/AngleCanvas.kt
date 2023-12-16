package mobappdev.example.sensorapplication.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.PointMode
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.unit.dp
import kotlin.reflect.KFunction2


@Composable
fun AngleCanvas(
    modifier: Modifier,
    setDimensions: (Float, Float) -> Unit,
    offsets: List<Offset>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(250.dp)
            .padding(14.dp)
            .border(2.dp, Color.Black),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Surface {
            Canvas(
                modifier = Modifier
                    .fillMaxSize()
                    .background(color = MaterialTheme.colorScheme.primaryContainer)
            ) {
                setDimensions(size.width, size.height) // kanske inte behövs
                drawPoints(
                    points = offsets,
                    pointMode = PointMode.Polygon,
                    color = Color.Blue,
                    strokeWidth = 4.dp.toPx(),
                    cap = StrokeCap.Round
                )


            }
        }

    }
}