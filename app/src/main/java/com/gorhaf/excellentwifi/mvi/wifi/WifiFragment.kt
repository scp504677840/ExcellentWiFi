package com.gorhaf.excellentwifi.mvi.wifi

import android.Manifest
import android.R
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import com.gorhaf.excellentwifi.databinding.FragmentWifiBinding

class WifiFragment : Fragment() {

    private var _binding: FragmentWifiBinding? = null
    private val binding get() = _binding!!
    private lateinit var wifiManager: WifiManager
    private lateinit var wifiListAdapter: ArrayAdapter<String>
    private val wifiNetworks = mutableListOf<String>()

    private val requestPermissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted: Boolean ->
            Log.d(TAG, "requestPermissionLauncher result: isGranted=$isGranted")
            if (isGranted) {
                Log.d(TAG, "ACCESS_FINE_LOCATION permission granted")
                startWifiScan()
            } else {
                Log.d(TAG, "ACCESS_FINE_LOCATION permission denied")
                Toast.makeText(requireContext(), "Location permission is required for WiFi scanning.", Toast.LENGTH_SHORT).show()
            }
        }

    private val wifiScanReceiver = object : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            Log.d(TAG, "wifiScanReceiver onReceive: ${intent.action}")
            if (intent.action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                val success = intent.getBooleanExtra(WifiManager.EXTRA_RESULTS_UPDATED, false)
                Log.d(TAG, "Scan results available, success: $success")
                if (success) {
                    scanSuccess()
                } else {
                    scanFailure()
                }
            }
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        Log.d(TAG, "onCreateView")
        _binding = FragmentWifiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Log.d(TAG, "onViewCreated")
        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        binding.wifiSwitch.isChecked = wifiManager.isWifiEnabled
        Log.d(TAG, "WiFi switch state: ${wifiManager.isWifiEnabled}")

        wifiListAdapter = ArrayAdapter(requireContext(), R.layout.simple_list_item_1, wifiNetworks)
        binding.wifiListView.adapter = wifiListAdapter

        binding.scanButtonWifi.setOnClickListener {
            Log.d(TAG, "WiFi scan button clicked")
            checkPermissionAndScan()
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        requireContext().registerReceiver(wifiScanReceiver, intentFilter)
        Log.d(TAG, "WiFi scan receiver registered")
    }

    private fun checkPermissionAndScan() {
        Log.d(TAG, "checkPermissionAndScan")
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            Log.d(TAG, "ACCESS_FINE_LOCATION permission already granted")
            startWifiScan()
        } else {
            Log.d(TAG, "Requesting ACCESS_FINE_LOCATION permission")
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startWifiScan() {
        Log.d(TAG, "startWifiScan")
        wifiNetworks.clear()
        wifiListAdapter.notifyDataSetChanged()
        val success = wifiManager.startScan()
        if (!success) {
            Log.w(TAG, "wifiManager.startScan() returned false")
            scanFailure()
        } else {
            Log.i(TAG, "WiFi scan initiated")
        }
        Toast.makeText(requireContext(), "Scanning for WiFi networks...", Toast.LENGTH_SHORT).show()
    }

    @SuppressLint("MissingPermission")
    private fun scanSuccess() {
        Log.d(TAG, "scanSuccess")
        val results: List<ScanResult> = wifiManager.scanResults
        Log.d(TAG, "Found ${results.size} WiFi networks")
        wifiNetworks.clear()
        for (result in results) {
            val networkInfo = "${result.SSID}\nSignal Strength: ${result.level} dBm"
            Log.d(TAG, "Network: $networkInfo")
            wifiNetworks.add(networkInfo)
        }
        wifiListAdapter.notifyDataSetChanged()
    }

    private fun scanFailure() {
        Log.e(TAG, "scanFailure")
        Toast.makeText(requireContext(), "WiFi scan failed.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        Log.d(TAG, "onDestroyView")
        requireContext().unregisterReceiver(wifiScanReceiver)
        Log.d(TAG, "WiFi scan receiver unregistered")
        _binding = null
    }

    companion object {
        private const val TAG = "WifiFragment"
    }
}