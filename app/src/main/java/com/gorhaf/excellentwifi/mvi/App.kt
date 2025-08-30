package com.gorhaf.excellentwifi.mvi

import android.app.Application
import android.util.Log
import com.gorhaf.excellentwifi.util.Mp4ToH264Converter
import com.gorhaf.excellentwifi.mvi.video.VideoDecoder
import com.gorhaf.excellentwifi.util.AssetFileUtils
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.io.File

class App : Application() {
    private val appCoroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    override fun onCreate() {
        super.onCreate()
        Log.i(TAG, "onCreate")
        VideoDecoder.INSTANCE.createMediaCodec()
        /*appCoroutineScope.launch {
            // 在后台线程调用
            Mp4ToH264Converter.convertMp4ToH264(
                inputMp4 = File(filesDir,"demo.mp4").canonicalPath,
                outputH264 = File(filesDir,"demo.h264").canonicalPath,
                writeSpsPpsBeforeIdr = true
            )
        }*/

        // 1) 复制单个文件（同步，UI 线程之外）
        // val dest = File(filesDir, "sample.txt")
        // AssetFileUtils.copyAssetFile(this, "docs/sample.txt", dest, overwrite = true)

        // 2) 递归复制 assets/myfolder -> filesDir/myassets
        // val targetDir = File(filesDir, "myassets")
        // AssetFileUtils.copyAssetsDirRecursive(this, "myfolder", targetDir, overwrite = false)

        // 3) 协程中使用（推荐）
        /*appCoroutineScope.launch {
            val dest = File(filesDir, "demo.mp4")
            AssetFileUtils.copyAssetFileSuspend(
                this@App,
                "demo.mp4",
                dest,
                overwrite = true
            ) { copied, total ->
                // 更新进度条（total == -1 表示未知）
            }
        }*/

    }

    companion object {
        private const val TAG = "App"
    }
}