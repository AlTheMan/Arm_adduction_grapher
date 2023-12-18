package mobappdev.example.sensorapplication.ui.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.RectangleShape
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun IconButton(
    modifier: Modifier = Modifier,
    iconId: Int,
    size: Dp,
    onClick: (Int) -> Unit,
    measurementId: Int
){
    Card(
        modifier = Modifier
            .size(size)
            .fillMaxSize()
            .padding(5.dp),
        onClick = {
            onClick(measurementId)
        },
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer,
        ),
        shape = RectangleShape,
        border = BorderStroke(1.dp, color = Color.Black)

    ){
        Row (modifier = Modifier.fillMaxSize(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center){
            Image(painter = painterResource(id = iconId), contentDescription = null, modifier = Modifier.fillMaxSize(0.7f))
        }
    }

}