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
    private val discoveredDeviceObjects = mutableListOf<BluetoothDevice>()

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
                    discoveredDeviceObjects.clear()
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
                        // Starting from Android 12 (API 31), BLUETOOTH_CONNECT is required to get the device name.
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
                            if (ContextCompat.checkSelfPermission(
                                    requireContext(),
                                    Manifest.permission.BLUETOOTH_CONNECT
                                ) != PackageManager.PERMISSION_GRANTED
                            ) {
                                Log.w(TAG, "BLUETOOTH_CONNECT permission not granted on API ${Build.VERSION.SDK_INT}, cannot get device name.")
                                return
                            }
                        }
                        // On older versions, BLUETOOTH permission is sufficient, which is already checked.
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val deviceName = it.name ?: "Unknown Device"
                        val deviceInfo = "$deviceName\n${it.address}\nRSSI: $rssi dBm"
                        Log.d(TAG, "Device found: $deviceInfo")
                        if (!discoveredDeviceObjects.contains(it)) {
                            discoveredDevices.add(deviceInfo)
                            discoveredDeviceObjects.add(it)
                            deviceListAdapter.notifyDataSetChanged()
                        }
                    }
                }
            }
        }
    }

    private val bondStateReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action
            if (action == BluetoothDevice.ACTION_BOND_STATE_CHANGED) {
                val device: BluetoothDevice? = intent.getParcelableExtra(BluetoothDevice.EXTRA_DEVICE)
                val bondState = intent.getIntExtra(BluetoothDevice.EXTRA_BOND_STATE, BluetoothDevice.ERROR)
                when (bondState) {
                    BluetoothDevice.BOND_BONDED -> {
                        Log.d(TAG, "Device ${device?.address} bonded.")
                        Toast.makeText(context, "Paired with ${device?.name ?: device?.address}", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_BONDING -> {
                        Log.d(TAG, "Device ${device?.address} bonding.")
                        Toast.makeText(context, "Pairing with ${device?.name ?: device?.address}...", Toast.LENGTH_SHORT).show()
                    }
                    BluetoothDevice.BOND_NONE -> {
                        val reason = intent.getIntExtra(BluetoothDevice.EXTRA_REASON, BluetoothDevice.ERROR)
                        val reasonString = getBondFailureReason(reason)
                        Log.e(TAG, "Device ${device?.address} not bonded. Reason: $reasonString ($reason)")
                        Toast.makeText(context, "Pairing failed: $reasonString", Toast.LENGTH_LONG).show()
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
        binding.devicesListView.setOnItemClickListener { _, _, position, _ ->
            Log.d(TAG, "Device list item clicked at position $position")
            // Cancel discovery because it's resource-intensive and can interfere with pairing.
            if (bluetoothAdapter?.isDiscovering == true) {
                Log.d(TAG, "Cancelling discovery to initiate pairing.")
                bluetoothAdapter?.cancelDiscovery()
            }

            val device = discoveredDeviceObjects[position]
            Log.d(TAG, "Attempting to pair with device: ${device.address}")

            // BLUETOOTH_CONNECT permission is required for createBond
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S && ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.BLUETOOTH_CONNECT) != PackageManager.PERMISSION_GRANTED) {
                Log.w(TAG, "Cannot create bond without BLUETOOTH_CONNECT permission.")
                Toast.makeText(requireContext(), "Permission to connect to Bluetooth devices is required.", Toast.LENGTH_SHORT).show()
                return@setOnItemClickListener
            }

            val bondInitiated = device.createBond()
            if (!bondInitiated) {
                Log.e(TAG, "Failed to initiate pairing with ${device.address}")
                Toast.makeText(requireContext(), "Failed to start pairing.", Toast.LENGTH_SHORT).show()
            }
        }

        binding.scanButton.setOnClickListener {
            Log.d(TAG, "Scan button clicked")
            checkPermissionsAndScan()
        }

        checkPermissionAndSetSwitch()
    }

    private fun getBondFailureReason(reason: Int): String {
        return when (reason) {
            BluetoothDevice.REASON_AUTH_CANCELED -> "Authentication Canceled"
            BluetoothDevice.REASON_AUTH_FAILED -> "Authentication Failed"
            BluetoothDevice.REASON_AUTH_REJECTED -> "Authentication Rejected"
            BluetoothDevice.REASON_REMOTE_DEVICE_DOWN -> "Remote Device Down"
            BluetoothDevice.REASON_DISCOVERY_IN_PROGRESS -> "Discovery in Progress"
            BluetoothDevice.REASON_REMOTE_USER_TERMINATED_CONNECTION -> "Remote User Terminated Connection"
            BluetoothDevice.REASON_LOCAL_HOST_TERMINATED_CONNECTION -> "Local Host Terminated Connection"
            else -> "Unknown Reason ($reason)"
        }
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

        val bondFilter = IntentFilter(BluetoothDevice.ACTION_BOND_STATE_CHANGED)
        requireActivity().registerReceiver(bondStateReceiver, bondFilter)
        Log.d(TAG, "Bond state receiver registered")
    }

    override fun onStop() {
        super.onStop()
        Log.d(TAG, "onStop")
        requireActivity().unregisterReceiver(discoveryReceiver)
        Log.d(TAG, "Discovery receiver unregistered")
        requireActivity().unregisterReceiver(bluetoothStateReceiver)
        Log.d(TAG, "Bluetooth state receiver unregistered")
        requireActivity().unregisterReceiver(bondStateReceiver)
        Log.d(TAG, "Bond state receiver unregistered")
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
