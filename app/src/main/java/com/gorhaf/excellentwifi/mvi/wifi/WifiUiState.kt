package com.gorhaf.excellentwifi.mvi.wifi

import kotlinx.coroutines.flow.MutableStateFlow

data class WifiUiState(
    val wifiNetworks: MutableStateFlow<List<String>> = MutableStateFlow(emptyList()),
    val isScanning: MutableStateFlow<Boolean> = MutableStateFlow(false),
    val isWifiEnabled: MutableStateFlow<Boolean> = MutableStateFlow(false)
)
