package com.example.musicplayer.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.musicplayer.R
import com.example.musicplayer.model.AudioTrack

class MusicRepository(private val context: Context) {

    fun getTracks(): List<AudioTrack> {

        val assetSongs = listOf(
            "bad_boy_song1.mp3",
            "one_republic_song2.mp3",
            "post_malone_song3.mp3",
            "david_guetta_song4.mp3",
            "g_funk_song5.mp3",
            "esDeeKid_song6.mp3",
            "instrumental_version.mp3"
        )

        return assetSongs.map { fileName ->

            val retriever = MediaMetadataRetriever()

            val afd = context.assets.openFd(fileName)

            retriever.setDataSource(
                afd.fileDescriptor,
                afd.startOffset,
                afd.length
            )

            val title = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_TITLE
            ) ?: fileName.removeSuffix(".mp3")

            val artist = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_ARTIST
            ) ?: "Unknown Artist"

            val duration = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toInt() ?: 0

            val artBytes = retriever.embeddedPicture

            val bitmap = artBytes?.let {
                BitmapFactory.decodeByteArray(it, 0, it.size)
            }

            retriever.release()

            AudioTrack(
                title = title,
                artist = artist,
                duration = duration,
                assetPath = fileName,
                albumArt = bitmap
            )
        }
    }
}