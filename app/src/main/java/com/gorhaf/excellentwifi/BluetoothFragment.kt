package com.gorhaf.excellentwifi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gorhaf.excellentwifi.databinding.FragmentBluetoothBinding

class BluetoothFragment : Fragment() {

    private var _binding: FragmentBluetoothBinding? = null
    private val binding get() = _binding!!
    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }
    private lateinit var deviceListAdapter: ArrayAdapter<String>
    private val discoveredDevices = mutableListOf<String>()

    private val permissionsLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) { permissions ->
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                updateBluetoothSwitch()
                startScan()
            } else {
                Toast.makeText(requireContext(), "Permissions are required for Bluetooth functionality.", Toast.LENGTH_SHORT).show()
            }
        }

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            when (intent.action) {
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            // Permissions are already checked before starting scan, but this is a safeguard
                            return
                        }
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val deviceName = it.name ?: "Unknown Device"
                        val deviceInfo = "$deviceName\n${it.address}\nRSSI: $rssi dBm"
                        if (!discoveredDevices.contains(deviceInfo)) {
                            discoveredDevices.add(deviceInfo)
                            deviceListAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        deviceListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, discoveredDevices)
        binding.devicesListView.adapter = deviceListAdapter

        binding.scanButton.setOnClickListener {
            checkPermissionsAndScan()
        }

        checkPermissionAndSetSwitch()
    }

    private fun checkPermissionsAndScan() {
        val requiredPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            requiredPermissions.add(Manifest.permission.BLUETOOTH)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            startScan()
        } else {
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startScan() {
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            // This check is mainly for the IDE, as permissions are handled by checkPermissionsAndScan
            return
        }
        if (bluetoothAdapter?.isDiscovering == true) {
            bluetoothAdapter?.cancelDiscovery()
        }
        discoveredDevices.clear()
        deviceListAdapter.notifyDataSetChanged()
        bluetoothAdapter?.startDiscovery()
        Toast.makeText(requireContext(), "Scanning for devices...", Toast.LENGTH_SHORT).show()
    }

    private fun checkPermissionAndSetSwitch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                permissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
            } else {
                updateBluetoothSwitch()
            }
        } else {
            updateBluetoothSwitch()
        }
    }

    private fun updateBluetoothSwitch() {
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                binding.bluetoothSwitch.isChecked = bluetoothAdapter?.isEnabled == true
            }
        } catch (e: SecurityException) {
            // Should not happen
        }
    }

    override fun onStart() {
        super.onStart()
        val filter = IntentFilter(BluetoothDevice.ACTION_FOUND)
        requireActivity().registerReceiver(discoveryReceiver, filter)
    }

    override fun onStop() {
        super.onStop()
        requireActivity().unregisterReceiver(discoveryReceiver)
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            // Handled by permissions check
            return
        }
        bluetoothAdapter?.cancelDiscovery()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
