package com.example.musicplayer.waveform

import android.content.Context
import android.media.MediaExtractor
import android.media.MediaFormat
import kotlin.math.abs

object WaveformExtractor {

    fun extract(context: Context, resId: Int, samples: Int = 200): List<Int> {

        val extractor = MediaExtractor()
        val afd = context.resources.openRawResourceFd(resId)

        extractor.setDataSource(
            afd.fileDescriptor,
            afd.startOffset,
            afd.length
        )

        afd.close()

        var trackIndex = -1

        for (i in 0 until extractor.trackCount) {
            val format = extractor.getTrackFormat(i)
            val mime = format.getString(MediaFormat.KEY_MIME) ?: continue
            if (mime.startsWith("audio/")) {
                trackIndex = i
                break
            }
        }

        if (trackIndex == -1) return emptyList()

        extractor.selectTrack(trackIndex)

        val buffer = ByteArray(1024)
        val amplitudes = mutableListOf<Int>()

        while (true) {
            val size = extractor.readSampleData(java.nio.ByteBuffer.wrap(buffer), 0)
            if (size < 0) break

            for (i in 0 until size step 2) {
                val value = (buffer[i].toInt() shl 8) or
                        (buffer[i + 1].toInt() and 0xFF)
                amplitudes.add(abs(value))
            }

            extractor.advance()
        }

        extractor.release()

        if (amplitudes.isEmpty()) return emptyList()

        // Downsample
        val chunkSize = amplitudes.size / samples
        if (chunkSize <= 0) return emptyList()

        return amplitudes.chunked(chunkSize)
            .map { chunk -> chunk.maxOrNull() ?: 0 }
    }
}