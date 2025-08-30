package com.gorhaf.excellentwifi.mvi.bt

import android.annotation.SuppressLint
import android.app.Application
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@SuppressLint("MissingPermission")
class BluetoothViewModel(application: Application) : AndroidViewModel(application) {

    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private val _uiState = MutableStateFlow(BluetoothUiState())
    val uiState = _uiState.asStateFlow()

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    _uiState.value.isScanning.value = true
                    _uiState.value.discoveredDevices.value = emptyList()
                    _uiState.value.discoveredDeviceObjects.clear()
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    _uiState.value.isScanning.value = false
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(
                                getApplication(),
                                android.Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            return
                        }
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val deviceName = it.name ?: "Unknown Device"
                        val deviceInfo = "$deviceName\n${it.address}\nRSSI: $rssi dBm"
                        val currentDevices = _uiState.value.discoveredDevices.value.toMutableList()
                        if (!currentDevices.contains(deviceInfo)) {
                            currentDevices.add(deviceInfo)
                            _uiState.value.discoveredDevices.value = currentDevices
                            _uiState.value.discoveredDeviceObjects.add(it)
                        }
                    }
                }
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                when (intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)) {
                    BluetoothDevice.BOND_BONDED -> {
                        Toast.makeText(context, "Paired with ${device?.name}", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        Toast.makeText(context, "Pairing with ${device?.name}", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> _uiState.value.isBluetoothEnabled.value = false
                    BluetoothAdapter.STATE_ON -> _uiState.value.isBluetoothEnabled.value = true
                }
            }
        }
    }

    init {
        registerReceivers()
        updateBluetoothState()
    }

    private fun registerReceivers() {
        val discoveryFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        getApplication<Application>().registerReceiver(discoveryReceiver, discoveryFilter)

        val bondFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bondStateReceiver, bondFilter)

        val stateFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        getApplication<Application>().registerReceiver(bluetoothStateReceiver, stateFilter)
    }

    fun startScan() {
        viewModelScope.launch {
            if (bluetoothAdapter?.isDiscovering == true) {
                bluetoothAdapter.cancelDiscovery()
            }
            bluetoothAdapter?.startDiscovery()
        }
    }

    fun pairDevice(device: BluetoothDevice) {
        try {
            device.createBond()
        } catch (e: SecurityException) {
            Log.e("BluetoothViewModel", "Failed to pair device", e)
        }
    }

    private fun updateBluetoothState() {
        _uiState.value.isBluetoothEnabled.value = bluetoothAdapter?.isEnabled == true
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(discoveryReceiver)
        getApplication<Application>().unregisterReceiver(bondStateReceiver)
        getApplication<Application>().unregisterReceiver(bluetoothStateReceiver)
    }
}
