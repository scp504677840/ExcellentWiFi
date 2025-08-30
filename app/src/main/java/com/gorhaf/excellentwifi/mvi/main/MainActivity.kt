package com.gorhaf.excellentwifi.mvi.main

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.gorhaf.excellentwifi.R

class MainActivity : AppCompatActivity() {

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