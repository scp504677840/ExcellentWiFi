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
import android.util.Log
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
            Log.d(TAG, "permissionsLauncher result: $permissions")
            val allPermissionsGranted = permissions.entries.all { it.value }
            if (allPermissionsGranted) {
                Log.d(TAG, "All permissions granted")
                updateBluetoothSwitch()
                startScan()
            } else {
                Log.d(TAG, "Some or all permissions denied")
                Toast.makeText(requireContext(), "Permissions are required for Bluetooth functionality.", Toast.LENGTH_SHORT).show()
            }
        }

    private val discoveryReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            Log.d(TAG, "discoveryReceiver onReceive: $action")
            when (action) {
                BluetoothAdapter.ACTION_DISCOVERY_STARTED -> {
                    Log.i(TAG, "Bluetooth discovery started.")
                    discoveredDevices.clear()
                    deviceListAdapter.notifyDataSetChanged()
                    binding.scanButton.text = "Scanning..."
                }
                BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> {
                    Log.i(TAG, "Bluetooth discovery finished.")
                    binding.scanButton.text = "Scan for Devices"
                }
                BluetoothDevice.ACTION_FOUND -> {
                    val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                    device?.let {
                        if (ActivityCompat.checkSelfPermission(
                                requireContext(),
                                Manifest.permission.BLUETOOTH_CONNECT
                            ) != PackageManager.PERMISSION_GRANTED
                        ) {
                            Log.w(TAG, "BLUETOOTH_CONNECT permission not granted, cannot get device name.")
                            return
                        }
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val deviceName = it.name ?: "Unknown Device"
                        val deviceInfo = "$deviceName\n${it.address}\nRSSI: $rssi dBm"
                        Log.d(TAG, "Device found: $deviceInfo")
                        if (!discoveredDevices.contains(deviceInfo)) {
                            discoveredDevices.add(deviceInfo)
                            deviceListAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private val bluetoothStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothAdapter.ACTION_STATE_CHANGED) {
                when (intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR)) {
                    BluetoothAdapter.STATE_OFF -> {
                        Log.d(TAG, "Bluetooth state is OFF")
                        binding.bluetoothSwitch.isChecked = false
                    }
                    BluetoothAdapter.STATE_ON -> {
                        Log.d(TAG, "Bluetooth state is ON")
                        binding.bluetoothSwitch.isChecked = true
                    }
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentBluetoothBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        deviceListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, discoveredDevices)
        binding.devicesListView.adapter = deviceListAdapter

        binding.scanButton.setOnClickListener {
            Log.d(TAG, "Scan button clicked")
            checkPermissionsAndScan()
        }

        checkPermissionAndSetSwitch()
    }

    private fun checkPermissionsAndScan() {
        Log.d(TAG, "checkPermissionsAndScan")
        val requiredPermissions = mutableListOf<String>()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            requiredPermissions.add(Manifest.permission.BLUETOOTH_SCAN)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_CONNECT)
        } else {
            requiredPermissions.add(Manifest.permission.BLUETOOTH)
            requiredPermissions.add(Manifest.permission.BLUETOOTH_ADMIN)
            requiredPermissions.add(Manifest.permission.ACCESS_FINE_LOCATION)
        }
        Log.d(TAG, "Required permissions: $requiredPermissions")

        val permissionsToRequest = requiredPermissions.filter {
            ContextCompat.checkSelfPermission(requireContext(), it) != PackageManager.PERMISSION_GRANTED
        }

        if (permissionsToRequest.isEmpty()) {
            Log.d(TAG, "All required permissions already granted")
            startScan()
        } else {
            Log.d(TAG, "Requesting permissions: $permissionsToRequest")
            permissionsLauncher.launch(permissionsToRequest.toTypedArray())
        }
    }

    private fun startScan() {
        Log.d(TAG, "startScan")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            Log.w(TAG, "BLUETOOTH_SCAN permission not granted, cannot start scan.")
            return
        }
        if (bluetoothAdapter?.isDiscovering == true) {
            Log.d(TAG, "Already discovering, cancelling previous discovery")
            bluetoothAdapter?.cancelDiscovery()
        }

        // Start discovery. The BroadcastReceiver will handle clearing the list and UI updates.
        val discoveryStarted = bluetoothAdapter?.startDiscovery()
        if (discoveryStarted == true) {
            Log.i(TAG, "Bluetooth discovery initiated successfully.")
            Toast.makeText(requireContext(), "Scanning for devices...", Toast.LENGTH_SHORT).show()
        } else {
            Log.e(TAG, "Failed to initiate Bluetooth discovery.")
            Toast.makeText(requireContext(), "Failed to start scan.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun checkPermissionAndSetSwitch() {
        Log.d(TAG, "checkPermissionAndSetSwitch")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.d(TAG, "Requesting BLUETOOTH_CONNECT for switch state")
                permissionsLauncher.launch(arrayOf(Manifest.permission.BLUETOOTH_CONNECT))
            } else {
                updateBluetoothSwitch()
            }
        } else {
            updateBluetoothSwitch()
        }
    }

    private fun updateBluetoothSwitch() {
        Log.d(TAG, "updateBluetoothSwitch")
        try {
            if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) == PackageManager.PERMISSION_GRANTED || Build.VERSION.SDK_INT < Build.VERSION_CODES.S) {
                val isEnabled = bluetoothAdapter?.isEnabled == true
                binding.bluetoothSwitch.isChecked = isEnabled
                Log.d(TAG, "Bluetooth switch updated to: $isEnabled")
            }
        } catch (e: SecurityException) {
            Log.e(TAG, "SecurityException in updateBluetoothSwitch", e)
        }
    }

    override fun onStart() {
        super.onStart()
        Log.d(TAG, "onStart")
        val discoveryFilter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_STARTED)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        requireActivity().registerReceiver(discoveryReceiver, discoveryFilter)
        Log.d(TAG, "Discovery receiver registered")

        val stateFilter = IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED)
        requireActivity().registerReceiver(bluetoothStateReceiver, stateFilter)
        Log.d(TAG, "Bluetooth state receiver registered")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        requireActivity().unregisterReceiver(discoveryReceiver)
        Log.d(TAG, "Discovery receiver unregistered")
        requireActivity().unregisterReceiver(bluetoothStateReceiver)
        Log.d(TAG, "Bluetooth state receiver unregistered")
        if (ActivityCompat.checkSelfPermission(
                requireContext(),
                Manifest.permission.BLUETOOTH_SCAN
            ) != PackageManager.PERMISSION_GRANTED && Build.VERSION.SDK_INT >= Build.VERSION_CODES.S
        ) {
            return
        }
        bluetoothAdapter?.cancelDiscovery()
        Log.d(TAG, "Bluetooth discovery cancelled")
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        _binding = null
    }

    companion object {
        private const val TAG = "BluetoothFragment"
    }
}
