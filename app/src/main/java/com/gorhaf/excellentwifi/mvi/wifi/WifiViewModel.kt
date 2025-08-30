package com.gorhaf.excellentwifi.mvi.wifi

import android.annotation.SuppressLint
import android.app.Application
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.AndroidViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

@SuppressLint("MissingPermission")
class WifiViewModel(application: Application) : AndroidViewModel(application) {

    private val wifiManager: WifiManager by lazy {
        application.applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
    }

    private val _uiState = MutableStateFlow(WifiUiState())
    val uiState = _uiState.asStateFlow()

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }
    }

    init {
        registerWifiReceiver()
        updateWifiState()
    }

    private fun registerWifiReceiver() {
        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        getApplication<Application>().registerReceiver(wifiScanReceiver, intentFilter)
    }

    fun startWifiScan() {
        _uiState.value.isScanning.value = true
        _uiState.value.wifiNetworks.value = emptyList()
        val success = wifiManager.startScan()
        if (!success) {
            scanFailure()
        }
        Toast.makeText(getApplication(), "Scanning for WiFi networks...", Toast.LENGTH_SHORT).show()
    }

    private fun scanSuccess() {
        val results: List<ScanResult> = wifiManager.scanResults
        val networkList = mutableListOf<String>()
        for (result in results) {
            val networkInfo = "${result.SSID}\nSignal Strength: ${result.level} dBm"
            networkList.add(networkInfo)
        }
        _uiState.value.wifiNetworks.value = networkList
        _uiState.value.isScanning.value = false
    }

    private fun scanFailure() {
        _uiState.value.isScanning.value = false
        Toast.makeText(getApplication(), "WiFi scan failed.", Toast.LENGTH_SHORT).show()
    }

    private fun updateWifiState() {
        _uiState.value.isWifiEnabled.value = wifiManager.isWifiEnabled
    }

    override fun onCleared() {
        super.onCleared()
        getApplication<Application>().unregisterReceiver(wifiScanReceiver)
    }

    companion object {
        private const val TAG = "WifiViewModel"
    }
}
