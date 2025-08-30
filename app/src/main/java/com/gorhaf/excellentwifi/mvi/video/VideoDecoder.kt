package com.gorhaf.excellentwifi.mvi.video

import android.graphics.SurfaceTexture
import android.media.MediaCodec
import android.media.MediaCodecInfo
import android.media.MediaFormat
import android.util.Log
import android.view.Surface

class VideoDecoder {
    private var codec: MediaCodec? = null

    fun createMediaCodec() {
        Log.i(TAG, "createMediaCodec")
        if (codec != null) {
            Log.i(TAG, "codec not null => skip")
            return
        }

        codec = MediaCodec.createDecoderByType("video/avc")

        val mime = "video/avc"
        val width = 720
        val height = 1449
        val frameRate = 30
        val format = MediaFormat.createVideoFormat(mime, width, height).apply {
            setInteger(
                MediaFormat.KEY_COLOR_FORMAT,
                MediaCodecInfo.CodecCapabilities.COLOR_FormatYUV420Flexible
            )
            setInteger(MediaFormat.KEY_FRAME_RATE, frameRate)
        }
        codec?.configure(format, createPlaceholderSurface(), null, 0)
        Log.i(TAG, "configure MediaCodec")
        codec?.start()
        Log.i(TAG, "start MediaCodec")
        // uiState.videoCoroutineScope.launch { inputFrame() }
        // uiState.videoCoroutineScope.launch { outputFrame() }
    }

    fun updateSurface(surface: Surface) {
        Log.i(TAG, "updateSurface")
        codec?.setOutputSurface(surface)
    }

    private suspend fun inputFrame() {
        while (true) {
            val inIndex = codec?.dequeueInputBuffer(0) ?: 0
            if (inIndex < 1) {
                continue
            }
            val inputBuffer = codec?.getInputBuffer(inIndex)

        }
    }

    private suspend fun outputFrame() {
        while (true) {
            // uiState.mediaCodec!!.dequeueOutputBuffer(uiState.mediaCodec!!.)
        }
    }

    // 创建一个临时 Surface（占位）
    private fun createPlaceholderSurface(): Surface {
        // 简单方法：用 SurfaceTexture -> Surface（注意：SurfaceTexture(0) 在部分设备/场景有风险，但通常可用）
        val st = SurfaceTexture(0).apply {
            // 不把它 attach 到 GL context，作为占位接收帧
            // 可选：setDefaultBufferSize(width, height)
        }
        return Surface(st)
    }

    companion object {
        private const val TAG = "VideoDecoder"

        val INSTANCE = VideoDecoder()
    }
}