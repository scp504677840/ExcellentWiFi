package com.gorhaf.excellentwifi.mvi.main

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gorhaf.excellentwifi.R
import com.gorhaf.excellentwifi.mvi.bt.BluetoothActivity
import com.gorhaf.excellentwifi.mvi.wifi.WifiActivity

class MainActivity : AppCompatActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var uiState: MainUiState

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d(TAG, "onCreate")
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_root_cl)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        initData()
        initView()
        initEvent()
    }

    /**
     * A native method that is implemented by the 'excellentwifi' native library,
     * which is packaged with this application.
     */
    external fun stringFromJNI(): String

    private fun initData() {
        Log.d(TAG, "initData")
        uiState = viewModel.uiState
    }

    private fun initView() {
        Log.d(TAG, "initView")
        uiState.goToBluetoothBtn = findViewById(R.id.main_go_to_bluetooth_btn)
        uiState.goToWifiBtn = findViewById(R.id.main_go_to_wifi_btn)
    }

    private fun initEvent() {
        Log.d(TAG, "initEvent")
        uiState.goToBluetoothBtn!!.setOnClickListener {
            startActivity(Intent(this, BluetoothActivity::class.java))
        }
        uiState.goToWifiBtn!!.setOnClickListener {
            startActivity(Intent(this, WifiActivity::class.java))
        }
    }

    companion object {
        private const val TAG = "MainActivity"

        // Used to load the 'excellentwifi' library on application startup.
        init {
            System.loadLibrary("excellentwifi")
        }
    }
}