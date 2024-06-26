package mobappdev.example.sensorapplication.ui

/**
 * File: MainActivity.kt
 * Purpose: Defines the main activity of the application.
 * Author:
 * Created: 2023-07-08
 * Last modified:
 */

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import dagger.hilt.android.AndroidEntryPoint
import mobappdev.example.sensorapplication.ui.screens.ExternalSensorScreen
import mobappdev.example.sensorapplication.ui.screens.HomeScreen
import mobappdev.example.sensorapplication.ui.screens.InternalSensorScreen
import mobappdev.example.sensorapplication.ui.screens.LoadedMeasurementScreen
import mobappdev.example.sensorapplication.ui.screens.PersistenceScreen
import mobappdev.example.sensorapplication.ui.theme.SensorapplicationTheme
import mobappdev.example.sensorapplication.ui.viewmodels.ExternalDataVM
import mobappdev.example.sensorapplication.ui.viewmodels.InternalDataVM
import mobappdev.example.sensorapplication.ui.viewmodels.PersistenceVM

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    // Todo: Change for your own deviceID
    private var deviceId = "B36B5C22" //B36B5C22  //B36B2C29

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Check and request storage permission
        checkAndRequestPermissions()

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
                val externalDataVM = hiltViewModel<ExternalDataVM>()
                val internalDataVM = hiltViewModel<InternalDataVM>()
                val persistenceVM = hiltViewModel<PersistenceVM>()
                // Use hardcoded deviceID
                externalDataVM.chooseSensor(deviceId)

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
                                },
                                onLoadButtonClicked = {
                                    navController.navigate("persistenceScreen")
                                }
                            )
                        }

                        composable("internalScreen") {
                            InternalSensorScreen(vm = internalDataVM)
                        }

                        composable("externalScreen") {
                            ExternalSensorScreen(vm = externalDataVM)
                        }
                        composable("persistenceScreen"){
                            PersistenceScreen(vm = persistenceVM, onMeasurementButtonClicked = {
                                persistenceVM.getMeasurement(it)
                                navController.navigate("loadedMeasurementScreen")
                            })
                        }
                        composable("loadedMeasurementScreen") {
                            LoadedMeasurementScreen(vm = persistenceVM)
                        }



                    }

                }
            }
        }
    }

    private fun checkAndRequestPermissions() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE), MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE)
        }
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                // Permission granted, proceed with file writing
                Log.d("Permissions", "WRITE_EXTERNAL_STORAGE permission granted")

            } else {
                // Permission denied, handle as appropriate
                Log.d("Permissions", "WRITE_EXTERNAL_STORAGE permission denied")
            }
        }
    }
    companion object {
        private const val MY_PERMISSIONS_REQUEST_WRITE_EXTERNAL_STORAGE = 1
    }
}


