package mobappdev.example.sensorapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import mobappdev.example.sensorapplication.R
import mobappdev.example.sensorapplication.ui.components.CardButton
import mobappdev.example.sensorapplication.ui.components.IconButton
import mobappdev.example.sensorapplication.ui.viewmodels.PersistenceVM

@Composable
fun PersistenceScreen(vm: PersistenceVM, onMeasurementButtonClicked: (Int) -> Unit) {
    val summaryList by vm.measurementSummary.collectAsState()

    LazyColumn(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.End) {
        items(summaryList) { it ->
            Row(modifier = Modifier.fillMaxWidth()) {
                Column(
                    modifier = Modifier.weight(0.8f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    CardButton(
                        buttonText = it.timeOfMeasurement.toString() + "  |  " + it.type.toString(),
                        enabled = true,
                        cardHeight = 50.dp
                    ) {
                        onMeasurementButtonClicked(it.id)
                    }
                }
                Column(
                    modifier = Modifier.weight(0.2f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    IconButton(
                        measurementId = it.id,
                        iconId = R.drawable.delete_24px,
                        size = 50.dp,
                        onClick = vm::deleteMeasurement
                    )
                }
            }
        }
    }


}