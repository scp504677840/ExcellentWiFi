package com.gorhaf.excellentwifi

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import com.gorhaf.excellentwifi.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private val bluetoothFragment = BluetoothFragment()
    private val wifiFragment = WifiFragment()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the default fragment
        loadFragment(bluetoothFragment)
        binding.bottomNavigation.selectedItemId = R.id.nav_bluetooth

        binding.bottomNavigation.setOnItemSelectedListener { item ->
            val selectedFragment: Fragment = when (item.itemId) {
                R.id.nav_bluetooth -> bluetoothFragment
                R.id.nav_wifi -> wifiFragment
                else -> bluetoothFragment
            }
            loadFragment(selectedFragment)
            true
        }
    }

    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .replace(R.id.fragment_container, fragment)
            .commit()
    }

    /**
     * A native method that is implemented by the 'excellentwifi' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    companion object {
        private const val TAG = "MainActivity"
        // Used to load the 'excellentwifi' library on application startup.
        init {
            System.loadLibrary("excellentwifi")
        }
    }
}