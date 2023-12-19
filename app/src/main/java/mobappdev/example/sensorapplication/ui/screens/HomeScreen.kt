package mobappdev.example.sensorapplication.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import mobappdev.example.sensorapplication.R


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    onInternalButtonClicked: () -> Unit,
    onExternalButtonClicked: () -> Unit,
    onLoadButtonClicked: () -> Unit
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally

    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.End,
            modifier = Modifier.fillMaxWidth().weight(0.2F).height(50.dp)
        ) {
            ClickableIconCard(
                icon = R.drawable.folder_24px,
                iconDescription = null,
                cardDescription = "Load",
                onCardClicked = onLoadButtonClicked
            )
        }
        Column(
            modifier = Modifier.background(color = MaterialTheme.colorScheme.background).weight(0.7F)
        ) {
            Row (modifier = Modifier.weight(0.5F)){
                ClickableIconCard(
                    modifier = Modifier,
                    icon = R.drawable.edgesensor_high_24px,
                    iconDescription = "Internal",
                    cardDescription = "Internal Sensor",
                    onInternalButtonClicked,
                )
            }
            Row (modifier = Modifier.weight(0.5F)){
                ClickableIconCard(
                    modifier = Modifier,
                    icon = R.drawable.bluetooth_24px,
                    iconDescription = "Bluetooth",
                    cardDescription = "Bluetooth Sensor",
                    onExternalButtonClicked,
                )
            }
        }
    }
}

@Composable
fun CardIcon(
    icon: Int, contentDescription: String?, cardDescription: String
) {

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Image(
            painter = painterResource(
                id = icon
            ), contentDescription = contentDescription, modifier = Modifier.fillMaxSize(0.7F)
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
    onCardClicked: () -> Unit,
) {
    Card(
        modifier = Modifier
            .padding(20.dp).aspectRatio(1F),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        onClick = onCardClicked,
        shape = RectangleShape,
        elevation = CardDefaults.elevatedCardElevation(
            defaultElevation = 10.dp,
        ),
        border = BorderStroke(2.dp, color = Color.Black)
    ) {
        CardIcon(
            icon = icon, contentDescription = iconDescription, cardDescription = cardDescription
        )
    }
}


