package com.example.musicplayer.repository

import android.content.Context
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import android.net.Uri
import com.example.musicplayer.R
import com.example.musicplayer.model.AudioTrack

class MusicRepository(private val context: Context) {

    fun getTracks(): List<AudioTrack> {

        val rawSongs = listOf(
            R.raw.bad_boy_song1,
            R.raw.one_republic_song2,
            R.raw.post_malone_song3
        )

        return rawSongs.map { resId ->

            val retriever = MediaMetadataRetriever()
            val uri = Uri.parse("android.resource://${context.packageName}/$resId")

            retriever.setDataSource(context, uri)

            val title = retriever.extractMetadata(
                MediaMetadataRetriever.METADATA_KEY_TITLE
            ) ?: context.resources.getResourceEntryName(resId)

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
                resId = resId,
                albumArt = bitmap
            )
        }
    }
}