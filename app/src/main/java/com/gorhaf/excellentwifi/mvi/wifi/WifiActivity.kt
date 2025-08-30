package com.gorhaf.excellentwifi.mvi.wifi

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.gorhaf.excellentwifi.databinding.ActivityWifiBinding
import kotlinx.coroutines.launch

class WifiActivity : AppCompatActivity() {

    private lateinit var binding: ActivityWifiBinding
    private val viewModel: WifiViewModel by viewModels()
    private lateinit var wifiNetworkAdapter: WifiNetworkAdapter

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                viewModel.startWifiScan()
            } else {
                Toast.makeText(this, "Location permission is required for WiFi scanning.", Toast.LENGTH_SHORT).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWifiBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()

        binding.scanButtonWifi.setOnClickListener {
            checkPermissionAndScan()
        }

        observeViewModel()
    }

    private fun setupRecyclerView() {
        wifiNetworkAdapter = WifiNetworkAdapter(emptyList())
        binding.wifiRecyclerView.apply {
            adapter = wifiNetworkAdapter
            layoutManager = LinearLayoutManager(this@WifiActivity)
        }
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                state.wifiNetworks.collect { networks ->
                    wifiNetworkAdapter.updateData(networks)
                }

                binding.wifiSwitch.isChecked = state.isWifiEnabled.value
                binding.scanButtonWifi.text = if (state.isScanning.value) "Scanning..." else "Scan for Networks"
                binding.scanButtonWifi.isEnabled = !state.isScanning.value
            }
        }
    }

    private fun checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            viewModel.startWifiScan()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }
}
