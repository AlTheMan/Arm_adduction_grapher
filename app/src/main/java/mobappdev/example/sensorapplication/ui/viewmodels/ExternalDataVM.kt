package mobappdev.example.sensorapplication.ui.viewmodels

/**
 * File: DataVM.kt
 * Purpose: Defines the view model of the data screen.
 *          Uses Dagger-Hilt to inject a controller model
 * Author:
 * Created: 2023-07-08
 * Last modified: 2023-07-11
 */

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.polar.sdk.api.model.PolarDeviceInfo
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import mobappdev.example.sensorapplication.domain.PolarController
import javax.inject.Inject

private const val LOG_TAG = "DataVM"

@HiltViewModel
class ExternalDataVM @Inject constructor(
    private val polarController: PolarController,
) : ViewModel() {
    val angleCurrentExternal = polarController.angleMeasurementCurrent
    private val _deviceList = MutableStateFlow<List<PolarDeviceInfo>>(listOf())
    val deviceList: StateFlow<List<PolarDeviceInfo>>
        get() = _deviceList.asStateFlow()
    private val _state = MutableStateFlow(DataUiState())
    val state = combine(
        polarController.connected, _state
    ) { connected, state ->
        state.copy(
            connected = connected,
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5000), _state.value)


    private var streamType: StreamType? = null
    private val _deviceId = MutableStateFlow("")


    val deviceId: StateFlow<String>
        get() = _deviceId.asStateFlow()


    fun chooseSensor(deviceId: String) {
        _deviceId.update { deviceId }
    }

    private fun connectToSensor() {
        polarController.connectToDevice(_deviceId.value)
    }

    fun chooseSensorAndConnect(deviceId: String) {
        _deviceId.update { deviceId }
        connectToSensor()
        closeBluetoothDialog()
    }
    fun disconnectFromSensor() {
        stopDataStream()
        polarController.disconnectFromDevice(_deviceId.value)
    }

    fun startExtAccAndGyro() {
        polarController.startAccAndGyroStream(_deviceId.value)
        streamType = StreamType.FOREIGN_ACC_AND_GYRO
        _state.update { it.copy(measuring = true) }
    }

    fun startExtAcc() {
        polarController.startAccStream(_deviceId.value)
        streamType = StreamType.FOREIGN_ACC
        _state.update { it.copy(measuring = true) }
    }

    fun startExtGyro() {
        polarController.startGyroStream(_deviceId.value)
        streamType = StreamType.FOREIGN_GYRO
        _state.update { it.copy(measuring = true) }
    }

    fun stopDataStream() {
        when (streamType) {
            StreamType.FOREIGN_HR -> polarController.stopHrStreaming()
            StreamType.FOREIGN_ACC -> polarController.stopAccStreaming()
            StreamType.FOREIGN_GYRO -> polarController.stopGyroStreaming()
            StreamType.FOREIGN_ACC_AND_GYRO -> {
                polarController.stopAccStreaming(); polarController.stopAccStreaming()
            }

            else -> {} // Do nothing
        }
        _state.update { it.copy(measuring = false) }
    }


    private fun deviceInList(polarDeviceInfo: PolarDeviceInfo): Boolean {
        for (element in _deviceList.value) {
            if (polarDeviceInfo.deviceId == element.deviceId) {
                return true
            }
        }
        return false
    }

    private fun searchBTDevices() {
        viewModelScope.launch {
            if (!state.value.isSearching) {
                _deviceList.value = emptyList()
            }
            _state.value = _state.value.copy(isSearching = polarController.searchBTDevices())
            Log.d(LOG_TAG, "IsSearching: ${_state.value.isSearching}")
        }
    }

    fun openBluetoothDialog() {
        _state.value = state.value.copy(showDialog = true)
        searchBTDevices()
    }

    fun closeBluetoothDialog() {
        _state.value = _state.value.copy(showDialog = false)
        searchBTDevices()
    }

    fun setSingleMeasurement() {
        _state.value = _state.value.copy(dualMeasurement = false)
    }

    fun setDualMeasurement() {
        _state.value = state.value.copy(dualMeasurement = true)
    }

    init {
        viewModelScope.launch {
            polarController.foundDevices.collect {
                    if (!deviceInList(it)) {
                        _deviceList.value = (_deviceList.value + it).toMutableList()
                    }
                }
        }
    }
}

data class DataUiState(
    val connected: Boolean = false,
    val measuring: Boolean = false,
    val showDialog: Boolean = false,
    val isSearching: Boolean = false,
    val dualMeasurement: Boolean = false
)

private enum class StreamType {
    FOREIGN_HR, FOREIGN_ACC, FOREIGN_GYRO, FOREIGN_ACC_AND_GYRO
}