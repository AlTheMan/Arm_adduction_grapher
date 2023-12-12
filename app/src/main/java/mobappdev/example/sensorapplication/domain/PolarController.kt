package mobappdev.example.sensorapplication.domain

/**
 * File: PolarController.kt
 * Purpose: Defines the blueprint for the polar controller model
 * Author: Jitse van Esch
 * Created: 2023-07-08
 * Last modified: 2023-07-11
 */


import com.polar.sdk.api.model.PolarAccelerometerData
import kotlinx.coroutines.flow.StateFlow

interface PolarController {
    val currentHR: StateFlow<Int?>
    val hrList: StateFlow<List<Int>>

    val accList: StateFlow<PolarAccelerometerData?>
    val currentAcc: StateFlow<PolarAccelerometerData.PolarAccelerometerDataSample?>

    val connected: StateFlow<Boolean>
    val measuring: StateFlow<Boolean>

    fun connectToDevice(deviceId: String)
    fun disconnectFromDevice(deviceId: String)

    fun startHrStreaming(deviceId: String)
    fun stopHrStreaming()

    fun startAccStream(deviceId: String)
    fun stopAccStreaming()

    }