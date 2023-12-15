package mobappdev.example.sensorapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CardButton(
    modifier: Modifier = Modifier,
    buttonText: String,
    enabled: Boolean,
    cardHeight: Dp,
    onButtonClick: () -> Unit,
){
    Card(
        modifier = Modifier
            .height(cardHeight)
            .padding(5.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        enabled = enabled,
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer),
        onClick = {
            onButtonClick()
        }

    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(text = buttonText)
        }
    }
}


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SingleDualCardButton(
    modifier: Modifier = Modifier,
    buttonText: String,
    enabled: Boolean,
    cardHeight: Dp,
    onButtonClick: () -> Unit,
){
    Card(
        modifier = Modifier
            .height(cardHeight)
            .padding(5.dp),
        shape = RectangleShape,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
            disabledContainerColor = MaterialTheme.colorScheme.surfaceVariant,
            disabledContentColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        enabled = enabled,
        border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer),
        onClick = {
            onButtonClick()
        }

    ) {
        Row(
            modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center) {
            Text(text = buttonText)
        }
    }
}
