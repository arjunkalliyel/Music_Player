package com.example.musicplayer.service

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.Build
import android.os.IBinder
import android.support.v4.media.MediaMetadataCompat
import android.support.v4.media.session.MediaSessionCompat
import android.support.v4.media.session.PlaybackStateCompat
import androidx.core.app.NotificationCompat
import com.example.musicplayer.musicPlayerManager.MusicPlayerManager
import com.example.musicplayer.R
import com.example.musicplayer.screens.MainActivity

class MusicService : Service() {

    private lateinit var mediaSession: MediaSessionCompat
    private val CHANNEL_ID = "MusicPlaybackChannel"
    private val NOTIFICATION_ID = 1

    companion object {
        const val ACTION_PLAY_PAUSE = "action_play_pause"
        const val ACTION_NEXT = "action_next"
        const val ACTION_PREV = "action_prev"

    }

    override fun onCreate() {
        super.onCreate()
        createNotificationChannel()

        mediaSession = MediaSessionCompat(this, "MusicService").apply {
            setCallback(object : MediaSessionCompat.Callback() {
                override fun onPlay() { MusicPlayerManager.instance?.resume() }
                override fun onPause() { MusicPlayerManager.instance?.pause() }
                override fun onSkipToNext() { MusicPlayerManager.instance?.playNext() }
                override fun onSkipToPrevious() { MusicPlayerManager.instance?.playPrevious() }
                override fun onSeekTo(pos: Long) { MusicPlayerManager.instance?.seekTo(pos.toInt()) }
            })
            isActive = true
        }

        // Listen for internal state changes to update the notification UI
        MusicPlayerManager.instance?.onServiceUpdate = {
            updateNotification()
        }

    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val manager = MusicPlayerManager.instance
        when (intent?.action) {
            ACTION_PLAY_PAUSE -> if (manager?.isPlaying() == true) manager.pause() else manager?.resume()
            ACTION_NEXT -> manager?.playNext()
            ACTION_PREV -> manager?.playPrevious()
        }
        updateNotification()
        return START_NOT_STICKY
    }

    private fun updateNotification() {
        val activityIntent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        }

        val contentPendingIntent = PendingIntent.getActivity(
            this,
            10,
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        val manager = MusicPlayerManager.instance ?: return
        val track = manager.getCurrentTrack() ?: return
        val isPlaying = manager.isPlaying()

        // Update Playback State
        val stateBuilder = PlaybackStateCompat.Builder()
            .setActions(
                PlaybackStateCompat.ACTION_PLAY or PlaybackStateCompat.ACTION_PAUSE or
                        PlaybackStateCompat.ACTION_SKIP_TO_NEXT or PlaybackStateCompat.ACTION_SKIP_TO_PREVIOUS or
                        PlaybackStateCompat.ACTION_SEEK_TO
            )
            .setState(
                if (isPlaying) PlaybackStateCompat.STATE_PLAYING else PlaybackStateCompat.STATE_PAUSED,
                manager.getCurrentPosition().toLong(),
                if (isPlaying) 1.0f else 0f
            )
        mediaSession.setPlaybackState(stateBuilder.build())

        // Update Metadata
        val metadataBuilder = MediaMetadataCompat.Builder()
            .putString(MediaMetadataCompat.METADATA_KEY_TITLE, track.title)
            .putString(MediaMetadataCompat.METADATA_KEY_ARTIST, track.artist)
            .putLong(MediaMetadataCompat.METADATA_KEY_DURATION, track.duration.toLong())

        track.albumArt?.let {
            metadataBuilder.putBitmap(MediaMetadataCompat.METADATA_KEY_ALBUM_ART, it)
        }
        mediaSession.setMetadata(metadataBuilder.build())

        // Build Pending Intents
        val playPauseIntent = Intent(this, MusicService::class.java).setAction(ACTION_PLAY_PAUSE)
        val playPause = PendingIntent.getService(this, 0, playPauseIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val prevIntent = Intent(this, MusicService::class.java).setAction(ACTION_PREV)
        val previous = PendingIntent.getService(this, 1, prevIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val nextIntent = Intent(this, MusicService::class.java).setAction(ACTION_NEXT)
        val next = PendingIntent.getService(this, 2, nextIntent, PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT)

        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_music_player)
            .setContentTitle(track.title)
            .setContentText(track.artist)
            .setLargeIcon(track.albumArt)
            .setContentIntent(contentPendingIntent)
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_previous, "Previous", previous))
            .addAction(NotificationCompat.Action(
                if (isPlaying) android.R.drawable.ic_media_pause else android.R.drawable.ic_media_play,
                "Play/Pause",
                playPause
            ))
            .addAction(NotificationCompat.Action(android.R.drawable.ic_media_next, "Next", next))
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setMediaSession(mediaSession.sessionToken)
                    .setShowActionsInCompactView(0, 1, 2)
            )

            .setOngoing(isPlaying)
            .build()

        startForeground(NOTIFICATION_ID, notification)

        // Allow user to swipe notification away if paused
        if (!isPlaying) {
            stopForeground(STOP_FOREGROUND_DETACH)
        }
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(CHANNEL_ID, "Music Playback", NotificationManager.IMPORTANCE_LOW)
            getSystemService(NotificationManager::class.java).createNotificationChannel(channel)
        }
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        super.onTaskRemoved(rootIntent)
        MusicPlayerManager.instance?.release()
        stopSelf()
    }

    override fun onDestroy() {
        super.onDestroy()
        mediaSession.release()
    }

    override fun onBind(intent: Intent?): IBinder? = null
}