package mobappdev.example.sensorapplication.ui.components

import androidx.compose.animation.core.tween
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import com.polar.sdk.api.model.PolarDeviceInfo

@OptIn(ExperimentalFoundationApi::class, ExperimentalMaterial3Api::class)
@Composable
fun BluetoothSearchDialog(
    devices: List<PolarDeviceInfo>, onCardClicked: (String) -> Unit, closeDialog: () -> Unit
) {
    Dialog(onDismissRequest = { closeDialog() }) {
        Surface(
            shape = RectangleShape,
            shadowElevation = 8.dp,
            border = BorderStroke(1.dp, color = MaterialTheme.colorScheme.onPrimaryContainer),
            color = MaterialTheme.colorScheme.primaryContainer
        ) {
            val scrollState = rememberScrollState()
            Column (modifier = Modifier.fillMaxWidth()){
                Row ( verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.Center,
                    modifier = Modifier.fillMaxWidth()){
                    Text(text = "Bluetooth Devices", style = MaterialTheme.typography.titleLarge)
                }
                LazyColumn(modifier = Modifier.height(250.dp)) {
                    items(devices) { it ->
                        Row(Modifier.animateItemPlacement(tween(durationMillis = 250))) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(60.dp)
                                    .padding(2.dp),
                                shape = RectangleShape,
                                border = BorderStroke(
                                    1.dp,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer
                                ),
                                elevation = CardDefaults.cardElevation(
                                    defaultElevation = 5.dp
                                ),
                                onClick = {onCardClicked(it.deviceId)}
                            ) {
                                Row (
                                    verticalAlignment = Alignment.CenterVertically,
                                    horizontalArrangement = Arrangement.Center,
                                    modifier = Modifier.fillMaxSize()
                                ){
                                    Text(text = it.name, style = MaterialTheme.typography.bodyLarge)
                                }
                            }

                        }

                    }
                }
            }
        }
    }
}