package mobappdev.example.sensorapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mobappdev.example.sensorapplication.ui.components.AngleCanvas
import mobappdev.example.sensorapplication.ui.components.CardButton
import mobappdev.example.sensorapplication.ui.viewmodels.PersistenceVM


@Composable
fun LoadedMeasurementScreen(vm: PersistenceVM){

    val offsets by vm.offsets.collectAsState()

    Column{
        AngleCanvas(modifier = Modifier, setDimensions = vm::setDimensions, offsets = offsets)
        CardButton(
            buttonText = "Export",
            enabled = true,
            cardHeight = 70.dp,
            onButtonClick = vm::export
        )
    }

}