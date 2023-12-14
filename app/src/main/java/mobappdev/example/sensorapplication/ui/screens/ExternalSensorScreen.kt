package mobappdev.example.sensorapplication.ui.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import mobappdev.example.sensorapplication.ui.viewmodels.DataVM

@Composable
fun ExternalSensorScreen ( vm: DataVM, modifier:Modifier){
    val state = vm.state.collectAsStateWithLifecycle().value
    val deviceId = vm.deviceId.collectAsStateWithLifecycle().value
    val deviceList by vm.deviceList.collectAsState()
    val angle by vm.angleCurrentExternal.collectAsState()

    Box(
        contentAlignment = Alignment.Center,
        //modifier = Modifier.weight(1f)
    ){
        Text(
            text = if(state.measuring) String.format("%.1f", angle?.angle?: 7f) else "-",
            fontSize = 54.sp,
            color = Color.Black,
        )
    }



}