package mobappdev.example.sensorapplication.ui.screens

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import mobappdev.example.sensorapplication.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(onInternalButtonClicked: () -> Unit, onExternalButtonClicked: () -> Unit) {

    Column(
        modifier = Modifier
            .background(color = MaterialTheme.colorScheme.background)
            .fillMaxSize(),
        verticalArrangement = Arrangement.SpaceEvenly,
        horizontalAlignment = Alignment.CenterHorizontally
    )
    {
        ClickableIconCard(
            Modifier.weight(1F),
            icon = R.drawable.edgesensor_high_24px,
            iconDescription = "Internal",
            cardDescription = "Internal Sensor",
            onInternalButtonClicked
        )
        ClickableIconCard(
            Modifier.weight(1F),
            icon = R.drawable.bluetooth_24px,
            iconDescription = "Bluetooth",
            cardDescription = "Bluetooth Sensor",
            onExternalButtonClicked
        )
    }
}

@Composable
fun CardIcon(
    icon: Int,
    contentDescription: String?,
    cardDescription: String
) {
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter =
            painterResource(
                id = icon
            ),
            contentDescription = contentDescription, modifier = Modifier.fillMaxSize(0.8F)
        )
        Text(
            text = cardDescription,
            style = MaterialTheme.typography.titleLarge,
            color = MaterialTheme.colorScheme.onPrimaryContainer
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ClickableIconCard(
    modifier: Modifier = Modifier,
    icon: Int,
    iconDescription: String?,
    cardDescription: String,
    onCardClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .aspectRatio(1F)
            .padding(50.dp),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        onClick = onCardClicked
    ) {
        CardIcon(
            icon = icon,
            contentDescription = iconDescription,
            cardDescription = cardDescription
        )
    }
}


