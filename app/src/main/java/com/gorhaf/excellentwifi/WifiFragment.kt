package com.gorhaf.excellentwifi

import android.Manifest
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
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
            if (isGranted) {
                startWifiScan()
            } else {
                Toast.makeText(requireContext(), "Location permission is required for WiFi scanning.", Toast.LENGTH_SHORT).show()
            }
        }

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

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWifiBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        wifiManager = requireContext().applicationContext.getSystemService(Context.WIFI_SERVICE) as WifiManager
        binding.wifiSwitch.isChecked = wifiManager.isWifiEnabled

        wifiListAdapter = ArrayAdapter(requireContext(), android.R.layout.simple_list_item_1, wifiNetworks)
        binding.wifiListView.adapter = wifiListAdapter

        binding.scanButtonWifi.setOnClickListener {
            checkPermissionAndScan()
        }

        val intentFilter = IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
        requireContext().registerReceiver(wifiScanReceiver, intentFilter)
    }

    private fun checkPermissionAndScan() {
        if (ContextCompat.checkSelfPermission(requireContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            startWifiScan()
        } else {
            requestPermissionLauncher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    private fun startWifiScan() {
        wifiNetworks.clear()
        wifiListAdapter.notifyDataSetChanged()
        val success = wifiManager.startScan()
        if (!success) {
            scanFailure()
        }
        Toast.makeText(requireContext(), "Scanning for WiFi networks...", Toast.LENGTH_SHORT).show()
    }

    private fun scanSuccess() {
        val results: List<ScanResult> = wifiManager.scanResults
        wifiNetworks.clear()
        for (result in results) {
            wifiNetworks.add("${result.SSID}\nSignal Strength: ${result.level} dBm")
        }
        wifiListAdapter.notifyDataSetChanged()
    }

    private fun scanFailure() {
        Toast.makeText(requireContext(), "WiFi scan failed.", Toast.LENGTH_SHORT).show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        requireContext().unregisterReceiver(wifiScanReceiver)
        _binding = null
    }
}
