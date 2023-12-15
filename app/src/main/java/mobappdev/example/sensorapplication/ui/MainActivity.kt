package mobappdev.example.sensorapplication.ui

/**
 * File: MainActivity.kt
 * Purpose: Defines the main activity of the application.
 * Author: Jitse van Esch
 * Created: 2023-07-08
 * Last modified: 2023-09-21
 */

import android.Manifest
import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import mobappdev.example.sensorapplication.ui.screens.BluetoothDataScreen
import mobappdev.example.sensorapplication.ui.screens.ExternalSensorScreen
import mobappdev.example.sensorapplication.ui.screens.HomeScreen
import mobappdev.example.sensorapplication.ui.screens.InternalSensorScreen
import mobappdev.example.sensorapplication.ui.theme.SensorapplicationTheme
import mobappdev.example.sensorapplication.ui.viewmodels.DataVM
import mobappdev.example.sensorapplication.ui.viewmodels.InternalDataVM

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Todo: Change for your own deviceID
    private var deviceId = "B36B5C22" //B36B5C22  //B36B2C29

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                requestPermissions(arrayOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT), 31)
            } else {
                requestPermissions(arrayOf(Manifest.permission.ACCESS_FINE_LOCATION), 30)
            }
        } else {
            requestPermissions(arrayOf(Manifest.permission.ACCESS_COARSE_LOCATION), 29)
        }

        setContent {
            SensorapplicationTheme {
                val dataVM = hiltViewModel<DataVM>()
                val internalDataVM = hiltViewModel<InternalDataVM>()
                // Use hardcoded deviceID
                dataVM.chooseSensor(deviceId)

                // A surface container using the 'background' color from the theme
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "homeScreen") {
                        composable("homeScreen") {
                            HomeScreen(
                                onInternalButtonClicked = {
                                    navController.navigate("internalScreen")
                                },
                                onExternalButtonClicked = {
                                    navController.navigate("externalScreen")
                                }
                            )
                        }

                        composable("internalScreen") {
                            InternalSensorScreen(vm = internalDataVM)
                        }

                        composable("externalScreen") {
                            ExternalSensorScreen(vm = dataVM)
                        }



                    }

                }
            }
        }
    }
}
