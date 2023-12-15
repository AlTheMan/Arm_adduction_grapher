package mobappdev.example.sensorapplication.ui.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CardElevation
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp


@Composable
fun InternalSensorScreen(){

    val testList: List<String> = listOf("one", "two", "three", "four")


    LazyColumn {
        items(testList) {
                it ->
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(2.dp),
                shape = RectangleShape,
                elevation = CardDefaults.cardElevation(
                    defaultElevation = 10.dp
                )) {
                Text(text = it)
            }
        }
    }


}

@Composable
@Preview
fun InternalSensorScreenPreview(){
    InternalSensorScreen()
}


