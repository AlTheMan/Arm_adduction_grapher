package mobappdev.example.sensorapplication.ui.screens

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobappdev.example.sensorapplication.ui.components.BluetoothSearchDialog
import mobappdev.example.sensorapplication.ui.components.CardButton
import mobappdev.example.sensorapplication.ui.components.SingleDualCardButton
import mobappdev.example.sensorapplication.ui.viewmodels.ExternalDataVM

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ExternalSensorScreen(vm: ExternalDataVM) {
    val state = vm.state.collectAsStateWithLifecycle().value
    val deviceId = vm.deviceId.collectAsStateWithLifecycle().value
    val deviceList by vm.deviceList.collectAsState()
    val angle by vm.angleCurrentExternal.collectAsState()
    
    Row {
        if (!state.connected)
            CardButton(
                modifier = Modifier,
                buttonText = "Search for devices",
                enabled = !state.isSearching,
                cardHeight = 50.dp,
                vm::openBluetoothDialog
            )
        else
            CardButton(
                modifier = Modifier,
                buttonText = "Disconnect $deviceId",
                enabled = true,
                cardHeight = 50.dp,
                vm::disconnectFromSensor
            )
    }
    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
        ) {
        if (state.showDialog) {
            BluetoothSearchDialog(devices = deviceList, onCardClicked = vm::chooseSensorAndConnect, closeDialog = vm::closeBluetoothDialog)
        }
        Text(
            text = if (state.measuring) String.format("%.1f", angle?.angle ?: 7f) else "-",
            fontSize = 54.sp,
            color = Color.Black,
        )
        Spacer(modifier = Modifier.height(200.dp))
        if (state.measuring) {
            CardButton(
                buttonText = "Stop",
                enabled = true,
                cardHeight = 100.dp,
                onButtonClick = vm::stopDataStream
            )
        } else {
            Row (horizontalArrangement = Arrangement.Center){
                Column(modifier = Modifier.weight(1F)) {
                    SingleDualCardButton(buttonText = "Single", enabled = state.dualMeasurement , cardHeight = 50.dp, onButtonClick = vm::setSingleMeasurement)
                }
                Column(modifier = Modifier.weight(1F)) {
                    SingleDualCardButton(
                        buttonText = "Dual",
                        enabled = !state.dualMeasurement,
                        cardHeight = 50.dp,
                        onButtonClick = vm::setDualMeasurement,
                    )
                }
            }
            CardButton(
                buttonText = "Start",
                enabled = state.connected,
                cardHeight = 100.dp,
                onButtonClick = if (state.dualMeasurement) vm::startExtAccAndGyro else vm::startExtAcc,
            )
        }
    }
}




