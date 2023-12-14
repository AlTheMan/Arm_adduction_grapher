package mobappdev.example.sensorapplication.domain

/**
 * File: PolarController.kt
 * Purpose: Defines the blueprint for the polar controller model
 * Author: Jitse van Esch
 * Created: 2023-07-08
 * Last modified: 2023-07-11
 */


import com.polar.sdk.api.model.PolarAccelerometerData

import com.polar.sdk.api.model.PolarGyroData

import com.polar.sdk.api.model.PolarDeviceInfo
import kotlinx.coroutines.flow.Flow

import kotlinx.coroutines.flow.StateFlow
import mobappdev.example.sensorapplication.data.AngleMeasurements

interface PolarController {
    val hrCurrent: StateFlow<Int?>
    val hrList: StateFlow<List<Int>>
    val devicesFlow: Flow<PolarDeviceInfo>
    val angleMeasurements: StateFlow<AngleMeasurements>
    val angleMeasurementCurrent: StateFlow<AngleMeasurements.measurment>
    val foundDevices: Flow<PolarDeviceInfo>

    val accList: StateFlow<PolarAccelerometerData?>
    val accCurrent: StateFlow<PolarAccelerometerData.PolarAccelerometerDataSample?>

    val gyrList: StateFlow<PolarGyroData?>
    val gyrCurrent: StateFlow<PolarGyroData.PolarGyroDataSample?>

    val connected: StateFlow<Boolean>
    val measuring: StateFlow<Boolean>

    fun connectToDevice(deviceId: String)
    fun disconnectFromDevice(deviceId: String)

    fun startHrStreaming(deviceId: String) //start streaming heart-rate measurements
    fun stopHrStreaming()


    suspend fun searchBTDevices()

    fun startAccStream(deviceId: String)

    fun stopAccStreaming()

    fun startGyroStream(deviceId: String) //start streaming gyroscope measurements
    fun stopGyroStreaming()

    }