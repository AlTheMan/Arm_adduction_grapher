package mobappdev.example.sensorapplication.ui.screens

import android.widget.Space
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import mobappdev.example.sensorapplication.ui.components.CardButton
import mobappdev.example.sensorapplication.ui.components.NumberPickerSlider
import mobappdev.example.sensorapplication.ui.components.SingleDualCardButton
import mobappdev.example.sensorapplication.ui.viewmodels.InternalDataVM


@Composable
fun InternalSensorScreen(vm: InternalDataVM) {

    val state by vm.internalUiState.collectAsState()
    val angle by vm.angleCurrentInternal.collectAsState()

    Column(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.Bottom,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {

        Text(
            text = if (state.measuring) String.format("%.1f", angle?.angle ?: 7f) else "-",
            fontSize = 54.sp,
            color = Color.Black,
        )



        Spacer(modifier = Modifier.height(200.dp))


        if (state.measuring) {
            Text(
                text = state.countDownTimer.toString(),
                fontSize = 54.sp,
                color = Color.Black,
            )
            Spacer(modifier = Modifier.height(100.dp))
        }
        else  {
            NumberPickerSlider(range = 10..31, selectedNumber = state.selectedTimerValue, onNumberSelected = vm::setTimerValue)
        }

        if (state.measuring) {
            CardButton(
                buttonText = "Stop",
                enabled = true,
                cardHeight = 100.dp,
                onButtonClick = vm::stopDataStream
            )
        } else {
            CardButton(
                buttonText = "Export",
                enabled = true,
                cardHeight = 70.dp,
                onButtonClick = vm::exportData
            )
            Row(horizontalArrangement = Arrangement.Center) {
                Column(modifier = Modifier.weight(1F)) {
                    SingleDualCardButton(
                        buttonText = "Single",
                        enabled = state.dualMeasurement,
                        cardHeight = 50.dp,
                        onButtonClick = vm::setSingleMeasurement
                    )
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
                enabled = true,
                cardHeight = 100.dp,
                onButtonClick = vm::startMeasurement
            )
        }
    }


}




