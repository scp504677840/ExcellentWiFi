package com.gorhaf.excellentwifi.mvi.bt

import android.bluetooth.BluetoothDevice
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

data class BluetoothUiState(
    val discoveredDevices: MutableStateFlow<List<String>> = MutableStateFlow(emptyList()),
    val discoveredDeviceObjects: MutableList<BluetoothDevice> = mutableListOf(),
    val isScanning: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val isBluetoothEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
)
