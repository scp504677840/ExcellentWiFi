package com.gorhaf.excellentwifi.util

import android.content.Context
import android.content.res.AssetFileDescriptor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.io.InputStream

object AssetFileUtils {
    private const val TAG = "AssetFileUtils"
    private const val DEFAULT_BUFFER_SIZE = 16 * 1024

    /**
     * 将 assets 下的单个文件复制到 app filesDir 下的目标文件（同步）。
     *
     * @param context 必需
     * @param assetPath assets 内路径，例如 "videos/sample.mp4" 或 "sample.txt"（不要以 "/" 开头）
     * @param destFile 目标文件（绝对路径 File），如果父目录不存在会自动创建
     * @param overwrite 是否覆盖已存在目标文件（true 则覆盖）
     * @param bufferSize IO 缓冲区大小（默认 16KB）
     * @param progress 可选回调: (copiedBytes, totalBytesOr-1IfUnknown)
     *
     * @throws IOException 读取或写入失败时抛出
     */
    @Throws(IOException::class)
    fun copyAssetFile(
        context: Context,
        assetPath: String,
        destFile: File,
        overwrite: Boolean = false,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        progress: ((copied: Long, total: Long) -> Unit)? = null
    ): File {
        if (destFile.exists()) {
            if (!overwrite) return destFile
            if (!destFile.delete()) {
                throw IOException("Cannot delete existing file: ${destFile.absolutePath}")
            }
        }
        destFile.parentFile?.let { if (!it.exists()) it.mkdirs() }

        val am = context.assets

        // 尝试获取总长度（可能失败，如果 asset 被压缩 openFd 会抛异常）
        var totalBytes = -1L
        try {
            val afd: AssetFileDescriptor = am.openFd(assetPath)
            totalBytes = afd.length
            afd.close()
        } catch (_: Exception) {
            // ignore, total unknown
        }

        am.open(assetPath).use { input ->
            FileOutputStream(destFile).use { fos ->
                copyStream(input, fos, bufferSize, progress, totalBytes)
            }
        }
        return destFile
    }

    /**
     * 将 assets 的某个目录（或单个文件）递归复制到 filesDir 下的目标目录（同步）。
     *
     * @param context 必需
     * @param assetsPath assets 内路径，目录或文件。如果复制整个 assets 根目录，请传 "" 或 null（建议指定目录）
     * @param destDir 目标目录（会被创建），例如 File(context.filesDir, "myassets")
     * @param overwrite 是否覆盖已存在的文件
     * @param progressPerFile 可选的每个文件复制进度回调 (assetPath, copied, total)
     *
     * @return 目标目录 File
     *
     * @throws IOException
     */
    @Throws(IOException::class)
    fun copyAssetsDirRecursive(
        context: Context,
        assetsPath: String?,
        destDir: File,
        overwrite: Boolean = false,
        progressPerFile: ((assetPath: String, copied: Long, total: Long) -> Unit)? = null
    ): File {
        val am = context.assets
        val base = assetsPath?.trim('/') ?: ""
        if (!destDir.exists()) destDir.mkdirs()

        val list = try {
            am.list(base)
        } catch (e: IOException) {
            throw IOException("Failed to list assets for path: $base", e)
        }

        // If list is null or empty -> might be a file (or an empty dir)
        if (list == null || list.isEmpty()) {
            // treat as file
            val destFile = File(destDir, base.substringAfterLast('/'))
            copyAssetFile(context, base, destFile, overwrite) { c, t ->
                progressPerFile?.invoke(base, c, t)
            }
            return destDir
        }

        // It's a directory: iterate entries
        for (name in list) {
            val childAssetPath = if (base.isEmpty()) name else "$base/$name"
            val childList = am.list(childAssetPath)
            if (childList != null && childList.isNotEmpty()) {
                // directory
                val subDest = File(destDir, name)
                if (!subDest.exists()) subDest.mkdirs()
                copyAssetsDirRecursive(context, childAssetPath, subDest, overwrite, progressPerFile)
            } else {
                // file
                val outFile = File(destDir, name)
                copyAssetFile(context, childAssetPath, outFile, overwrite) { c, t ->
                    progressPerFile?.invoke(childAssetPath, c, t)
                }
            }
        }
        return destDir
    }

    /**
     * suspend 版本：在 IO 线程执行的 copyAssetFile。
     */
    suspend fun copyAssetFileSuspend(
        context: Context,
        assetPath: String,
        destFile: File,
        overwrite: Boolean = false,
        bufferSize: Int = DEFAULT_BUFFER_SIZE,
        progress: ((copied: Long, total: Long) -> Unit)? = null
    ): File = withContext(Dispatchers.IO) {
        copyAssetFile(context, assetPath, destFile, overwrite, bufferSize, progress)
    }

    /**
     * suspend 版本：递归复制目录
     */
    suspend fun copyAssetsDirRecursiveSuspend(
        context: Context,
        assetsPath: String?,
        destDir: File,
        overwrite: Boolean = false,
        progressPerFile: ((assetPath: String, copied: Long, total: Long) -> Unit)? = null
    ): File = withContext(Dispatchers.IO) {
        copyAssetsDirRecursive(context, assetsPath, destDir, overwrite, progressPerFile)
    }

    // ==== internal helper ====
    @Throws(IOException::class)
    private fun copyStream(
        input: InputStream,
        fos: FileOutputStream,
        bufferSize: Int,
        progress: ((copied: Long, total: Long) -> Unit)?,
        totalBytes: Long
    ) {
        val buffer = ByteArray(bufferSize.coerceAtLeast(4 * 1024))
        var read: Int
        var copied: Long = 0
        while (true) {
            read = input.read(buffer)
            if (read <= 0) break
            fos.write(buffer, 0, read)
            copied += read
            progress?.invoke(copied, totalBytes)
        }
        fos.flush()
    }
}
