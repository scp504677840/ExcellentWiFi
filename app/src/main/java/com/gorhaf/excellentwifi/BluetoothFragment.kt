package com.gorhaf.excellentwifi

import android.Manifest
import android.bluetooth.BluetoothAdapter
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gorhaf.excellentwifi.databinding.FragmentBluetoothBinding

class BluetoothFragment : Fragment() {

    private var _binding: FragmentBluetoothBinding? = null
    private val binding get() = _binding!!
    private val bluetoothAdapter: BluetoothAdapter? by lazy { BluetoothAdapter.getDefaultAdapter() }

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            if (isGranted) {
                updateBluetoothSwitch()
            } else {
                // Handle the case where the user denies the permission.
                // For now, we'll just leave the switch as is.
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
        checkPermissionAndSetSwitch()
    }

    private fun checkPermissionAndSetSwitch() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            when {
                ContextCompat.checkSelfPermission(
                    requireContext(),
                    Manifest.permission.BLUETOOTH_CONNECT
                ) == PackageManager.PERMISSION_GRANTED -> {
                    updateBluetoothSwitch()
                }
                else -> {
                    requestPermissionLauncher.launch(Manifest.permission.BLUETOOTH_CONNECT)
                }
            }
        } else {
            updateBluetoothSwitch()
        }
    }

    private fun updateBluetoothSwitch() {
        // The BLUETOOTH_CONNECT permission is required to check isEnabled().
        // Since we are checking for it, we can suppress the lint warning.
        try {
            binding.bluetoothSwitch.isChecked = bluetoothAdapter?.isEnabled == true
        } catch (e: SecurityException) {
            // This should not happen if permission is granted, but as a safeguard.
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
