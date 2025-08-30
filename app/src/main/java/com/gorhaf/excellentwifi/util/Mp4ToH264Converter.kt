package com.gorhaf.excellentwifi.util

import android.media.MediaExtractor
import android.media.MediaFormat
import android.util.Log
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.nio.ByteBuffer

object Mp4ToH264Converter {
    private const val TAG = "Mp4ToH264Converter"
    private val ANNEXB_START = byteArrayOf(0x00, 0x00, 0x00, 0x01)

    /**
     * 将 inputMp4 中的第一个 H.264(video/avc) track 提取为裸 h264 文件（Annex-B）。
     *
     * @param inputMp4 输入 MP4 文件路径
     * @param outputH264 输出 .h264 文件路径（若存在会覆盖）
     * @param writeSpsPpsBeforeIdr 是否在每个关键帧前写入 SPS/PPS（提高兼容性, 默认 true）
     *
     * @throws java.io.IOException on file/IO errors or IllegalArgumentException if no h264 track found.
     */
    @Throws(IOException::class)
    fun convertMp4ToH264(
        inputMp4: String,
        outputH264: String,
        writeSpsPpsBeforeIdr: Boolean = true
    ) {
        val extractor = MediaExtractor()
        try {
            extractor.setDataSource(inputMp4)
        } catch (e: Exception) {
            extractor.release()
            throw IOException("setDataSource failed for $inputMp4", e)
        }

        // 找到第一个 video/avc track
        var trackIndex = -1
        var format: MediaFormat? = null
        for (i in 0 until extractor.trackCount) {
            val f = extractor.getTrackFormat(i)
            val mime = f.getString(MediaFormat.KEY_MIME)
            if (mime != null && mime.startsWith("video/") && mime.contains("avc")) {
                trackIndex = i
                format = f
                break
            }
        }
        if (trackIndex < 0 || format == null) {
            extractor.release()
            throw IllegalArgumentException("No video/avc track found in $inputMp4")
        }

        // 解析 csd-0 (avcC) 以获得 lengthSize（lengthSizeMinusOne + 1），并获取 sps/pps
        val csd0 = format.getByteBuffer("csd-0")
        val csd1 = format.getByteBuffer("csd-1")

        val lengthSize = parseAvcCToNalLengthSize(csd0) // 默认返回 4 如果无法解析
        Log.i(TAG, "Found avcC lengthSize=$lengthSize")

        // 准备输出文件
        val outFile = File(outputH264)
        if (outFile.exists()) outFile.delete()
        val fos = FileOutputStream(outFile)

        try {
            // 写入起始的 SPS/PPS（如果有）
            if (csd0 != null) {
                // csd-0 实际包含 avcC header + SPS/PPS，不能直接写整个 csd0
                // 我们从 csd-0 里提取 sps/pps 用 annex-b 写入
                writeSpsPpsFromAvcC(csd0, fos)
            } else {
                // 若没有 csd-0，但 format 可能有 csd-1? try write csd-1 only
                if (csd1 != null) {
                    writeAnnexBPacket(csd1, fos)
                }
            }

            // 准备样本读取 buffer —— 尝试使用媒体格式的 MAX_INPUT_SIZE，否则用 1MB
            val defaultMax = 1 * 1024 * 1024
            val maxInputSize = try {
                if (format.containsKey(MediaFormat.KEY_MAX_INPUT_SIZE)) format.getInteger(
                    MediaFormat.KEY_MAX_INPUT_SIZE) else defaultMax
            } catch (e: Exception) {
                defaultMax
            }
            var bufferSize = maxInputSize.coerceAtLeast(128 * 1024)
            var buffer = ByteArray(bufferSize)
            val bb = ByteBuffer.wrap(buffer)

            // 开始读取样本
            extractor.selectTrack(trackIndex)
            while (true) {
                // 读取 sample
                val sampleSize = extractor.readSampleData(bb, 0)
                if (sampleSize < 0) {
                    // EOF
                    break
                }

                // 若 buffer 不够大则扩容
                if (sampleSize > buffer.size) {
                    bufferSize = sampleSize
                    buffer = ByteArray(bufferSize)
                    bb.clear()
                    // 重新 wrap
                    bb.put(buffer, 0, 0)
                }

                // 拷贝数据到一个独立的 byte[]（避免 ByteBuffer limit/position 问题）
                val sampleData = ByteArray(sampleSize)
                bb.rewind()
                bb.limit(sampleSize)
                bb.get(sampleData, 0, sampleSize)
                bb.clear()

                // 判断当前样本是否为关键帧（sync/sample flag）——这里优先使用 extractor.sampleFlags（API 支持）
                val flags = try {
                    extractor.sampleFlags
                } catch (e: Throwable) {
                    0
                }
                val isKeyFrame = (flags and MediaExtractor.SAMPLE_FLAG_SYNC.toInt()) != 0

                // 如果需要在 IDR 前写入 sps/pps，检查样本是否含 IDR（NAL type 5）或样本为同步样本
                var sampleHasIdr = false
                // 若 lengthSize 足够且样本可以解析，则检查第一个 NAL 的类型
                if (lengthSize > 0 && sampleSize >= lengthSize) {
                    var offset = 0
                    // 只检查第一个 nal
                    if (sampleSize >= lengthSize) {
                        val nalLen = readNalLength(sampleData, 0, lengthSize)
                        if (nalLen > 0 && nalLen <= sampleSize - lengthSize) {
                            val nalHeader = sampleData[lengthSize].toInt() and 0x1F
                            if (nalHeader == 5) sampleHasIdr = true
                        }
                    }
                }

                if (writeSpsPpsBeforeIdr && (isKeyFrame || sampleHasIdr)) {
                    // 把 sps/pps 再写到文件（如果 csd0 可用）
                    if (csd0 != null) {
                        writeSpsPpsFromAvcC(csd0, fos)
                    } else if (csd1 != null) {
                        writeAnnexBPacket(csd1, fos)
                    }
                }

                // 把 sample 中的 length-prefixed NALs 转为 Annex-B 写入
                convertLengthPrefixedToAnnexBAndWrite(sampleData, 0, sampleSize, lengthSize, fos)

                extractor.advance()
            }

            fos.flush()
            Log.i(TAG, "convertMp4ToH264 completed: $outputH264")
        } finally {
            try { fos.close() } catch (_: Throwable) {}
            extractor.release()
        }
    }

    // 将 avcC (csd-0) 里的 SPS/PPS 提取并以 Annex-B 写入到 fos
    private fun writeSpsPpsFromAvcC(csd0: ByteBuffer, fos: FileOutputStream) {
        val arr = ByteArray(csd0.remaining())
        csd0.mark()
        csd0.get(arr)
        csd0.reset()

        // avcC 格式:
        // configurationVersion(1) + AVCProfileIndication(1) + profile_compatibility(1) + AVCLevelIndication(1)
        // lengthSizeMinusOne (1, 下两位)
        // numOfSequenceParameterSets (1, 下5位)
        // for each SPS: sequenceParameterSetLength (2) + SPS data
        // numOfPictureParameterSets (1)
        // for each PPS: pictureParameterSetLength (2) + PPS data

        var offset = 0
        if (arr.size < 7) {
            // 不可解析，直接写整个 csd0 体（可能包含 header + sps/pps）作为兜底（加 start code）
            writeAnnexBPacket(ByteBuffer.wrap(arr), fos)
            return
        }
        offset += 4 // skip first 4 bytes
        val lengthSizeMinusOne = arr[offset].toInt() and 0x03
        offset++
        val numSps = arr[offset].toInt() and 0x1F
        offset++

        // read SPS
        for (i in 0 until numSps) {
            if (offset + 2 > arr.size) return
            val spsLen = ((arr[offset].toInt() and 0xFF) shl 8) or (arr[offset + 1].toInt() and 0xFF)
            offset += 2
            if (offset + spsLen > arr.size) return
            val sps = arr.copyOfRange(offset, offset + spsLen)
            fos.write(ANNEXB_START)
            fos.write(sps)
            offset += spsLen
        }

        // num of PPS
        if (offset >= arr.size) return
        val numPps = arr[offset].toInt() and 0xFF
        offset++
        for (i in 0 until numPps) {
            if (offset + 2 > arr.size) return
            val ppsLen = ((arr[offset].toInt() and 0xFF) shl 8) or (arr[offset + 1].toInt() and 0xFF)
            offset += 2
            if (offset + ppsLen > arr.size) return
            val pps = arr.copyOfRange(offset, offset + ppsLen)
            fos.write(ANNEXB_START)
            fos.write(pps)
            offset += ppsLen
        }
    }

    // 写入单个 ByteBuffer（或 ByteArray）为 Annex-B 包（仅添加 start code + payload）
    private fun writeAnnexBPacket(bb: ByteBuffer, fos: FileOutputStream) {
        val arr = ByteArray(bb.remaining())
        bb.mark()
        bb.get(arr)
        bb.reset()
        fos.write(ANNEXB_START)
        fos.write(arr)
    }

    private fun writeAnnexBPacket(arr: ByteArray?, fos: FileOutputStream) {
        if (arr == null) return
        fos.write(ANNEXB_START)
        fos.write(arr)
    }

    // 将 length-prefixed NALs 转成 Annex-B 写入输出流
    private fun convertLengthPrefixedToAnnexBAndWrite(
        data: ByteArray,
        offsetStart: Int,
        totalSize: Int,
        lengthSize: Int,
        fos: FileOutputStream
    ) {
        var offset = offsetStart
        val end = offsetStart + totalSize
        while (offset + lengthSize <= end) {
            val nalLen = readNalLength(data, offset, lengthSize)
            offset += lengthSize
            if (nalLen <= 0 || offset + nalLen > end) {
                // 防御性处理：如果长度不合理，写剩余全部作为单一 NAL
                val remaining = end - offset
                if (remaining > 0) {
                    fos.write(ANNEXB_START)
                    fos.write(data, offset, remaining)
                }
                break
            }
            // write start code + nal
            fos.write(ANNEXB_START)
            fos.write(data, offset, nalLen)
            offset += nalLen
        }
    }

    // 从 avcC 的 csd-0 解析 lengthSize（默认 4）
    private fun parseAvcCToNalLengthSize(csd0: ByteBuffer?): Int {
        if (csd0 == null) return 4
        return try {
            val arr = ByteArray(csd0.remaining())
            csd0.mark()
            csd0.get(arr)
            csd0.reset()
            if (arr.size >= 5) {
                val lengthSizeMinusOne = arr[4].toInt() and 0x03
                lengthSizeMinusOne + 1
            } else 4
        } catch (e: Exception) {
            4
        }
    }

    // 从 byte array 读取 NAL length（big-endian），lengthFieldBytes 可为 1/2/4
    private fun readNalLength(data: ByteArray, offset: Int, lengthFieldBytes: Int): Int {
        var len = 0
        for (i in 0 until lengthFieldBytes) {
            len = (len shl 8) or (data[offset + i].toInt() and 0xFF)
        }
        return len
    }
}