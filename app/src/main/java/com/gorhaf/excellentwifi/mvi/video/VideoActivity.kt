package com.gorhaf.excellentwifi.mvi.video

import android.content.Context
import android.content.Intent
import android.content.res.Configuration
import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.os.Bundle
import android.util.Log
import android.view.Surface
import android.view.TextureView
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import kotlinx.coroutines.Runnable
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

class VideoActivity : AppCompatActivity() {
    private val viewModel: VideoViewModel by viewModels()
    private lateinit var uiState: VideoUiState

    private val surfaceTextureListener = object : TextureView.SurfaceTextureListener {
        override fun onSurfaceTextureAvailable(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            Log.i(TAG, "onSurfaceTextureAvailable: ${surface == null}/$width/$height")
            uiState.decoder!!.updateSurface(Surface(surface))
        }

        override fun onSurfaceTextureDestroyed(surface: SurfaceTexture): Boolean {
            Log.i(TAG, "onSurfaceTextureDestroyed")
            return true
        }

        override fun onSurfaceTextureSizeChanged(
            surface: SurfaceTexture,
            width: Int,
            height: Int
        ) {
            Log.i(TAG, "onSurfaceTextureSizeChanged: ${surface == null}/$width/$height")
        }

        override fun onSurfaceTextureUpdated(surface: SurfaceTexture) {
            Log.i(TAG, "onSurfaceTextureUpdated")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // enableEdgeToEdge()
        val textureView = TextureView(this)
        textureView.surfaceTextureListener = surfaceTextureListener
        setContentView(textureView)
        /*ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }*/
        uiState = viewModel.uiState
        Log.i(TAG, "onCreate: ${viewModel == null}")
        Log.i(TAG, "onCreate: ${uiState == null}")
        initData()
        initView()
        initEvent()
    }

    override fun onStart() {
        super.onStart()
        Log.i(TAG, "onStart")
    }

    override fun onResume() {
        super.onResume()
        Log.i(TAG, "onResume")
    }

    override fun onPause() {
        super.onPause()
        Log.i(TAG, "onPause")
    }

    override fun onStop() {
        super.onStop()
        Log.i(TAG, "onStop")
    }

    override fun onDestroy() {
        super.onDestroy()
        uiState.videoCoroutineScope.cancel("onDestroy")
        Log.i(TAG, "onDestroy")
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        Log.i(TAG, "onNewIntent")
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        Log.i(TAG, "onConfigurationChanged")
    }

    private fun initData() {
        Log.i(TAG, "initData")
        uiState.decoder = VideoDecoder.INSTANCE
        uiState.decoder?.createMediaCodec()
    }

    private fun initView() {
        Log.i(TAG, "initView")
    }

    private fun initEvent() {
        Log.i(TAG, "initEvent")
    }

    companion object {
        private const val TAG = "VideoActivity"

        fun startActivity(context: Context) {
            val intent = Intent(context, VideoActivity::class.java)
            context.startActivity(intent)
            Log.i(TAG, "startActivity")
        }
    }
}