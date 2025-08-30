package com.gorhaf.excellentwifi.mvi.bt

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.lifecycle.lifecycleScope
import com.gorhaf.excellentwifi.databinding.ActivityBluetoothBinding
import kotlinx.coroutines.launch

class BluetoothActivity : AppCompatActivity() {

    private lateinit var binding: ActivityBluetoothBinding
    private val viewModel: BluetoothViewModel by viewModels()
    private lateinit var deviceListAdapter: ArrayAdapter<String>

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                viewModel.startScan()
            } else {
                Toast.makeText(this, "Permissions are required for Bluetooth functionality.", Toast.LENGTH_SHORT).show()
            }
        }

    @SuppressLint("MissingPermission")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityBluetoothBinding.inflate(layoutInflater)
        setContentView(binding.root)

        deviceListAdapter = ArrayAdapter(this, R.layout.simple_list_item_1, mutableListOf())
        binding.devicesListView.adapter = deviceListAdapter

        binding.devicesListView.setOnItemClickListener { _, _, position, _ ->
            val device = viewModel.uiState.value.discoveredDeviceObjects[position]
            viewModel.pairDevice(device)
        }

        binding.scanButton.setOnClickListener {
            checkPermissionsAndScan()
        }

        observeViewModel()
    }

    private fun observeViewModel() {
        lifecycleScope.launch {
            viewModel.uiState.collect { state ->
                deviceListAdapter.clear()
                deviceListAdapter.addAll(state.discoveredDevices.value)
                deviceListAdapter.notifyDataSetChanged()

                binding.bluetoothSwitch.isChecked = state.isBluetoothEnabled.value
                binding.scanButton.text = if (state.isScanning.value) "Scanning..." else "Scan for Devices"
            }
        }
    }

    private fun checkPermissionsAndScan() {
        val requiredPermissions = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            listOf(Manifest.permission.BLUETOOTH_SCAN, Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            listOf(
                Manifest.permission.BLUETOOTH,
                Manifest.permission.BLUETOOTH_ADMIN,
                Manifest.permission.ACCESS_FINE_LOCATION
            )
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(this, it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            viewModel.startScan()
        } else {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }
}
