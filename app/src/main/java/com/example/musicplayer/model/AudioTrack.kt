package com.example.musicplayer.model

import android.graphics.Bitmap

data class AudioTrack(
    val title: String,
    val artist: String,
    val duration: Int,
    val assetPath: String,
    val albumArt: Bitmap?
)

