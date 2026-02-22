package com.example.musicplayer.model

import android.graphics.Bitmap

data class AudioTrack(
    val title: String,
    val artist: String,
    val duration: Int,
    val resId: Int,
    val albumArt: Bitmap? = null
)

