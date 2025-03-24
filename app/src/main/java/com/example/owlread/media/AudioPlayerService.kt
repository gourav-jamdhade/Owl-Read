package com.example.owlread.media

import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.media.AudioFocusRequest
import android.media.AudioManager
import android.os.Binder
import android.os.Build
import android.os.IBinder
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.ExoPlayer
import com.example.owlread.MainActivity
import com.example.owlread.R

class AudioPlayerService : Service() {

    private var exoPlayer: ExoPlayer? = null
    private lateinit var audioManager: AudioManager
    private var audioFocusRequest: AudioFocusRequest? = null


    // Custom Binder class to expose the service instance
    inner class LocalBinder : Binder() {
        fun getService(): AudioPlayerService = this@AudioPlayerService
    }

    private val binder = LocalBinder()


    override fun onCreate() {
        super.onCreate()
        exoPlayer = ExoPlayer.Builder(this).build()
        audioManager = getSystemService(Context.AUDIO_SERVICE) as AudioManager
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        val audioUrl = intent?.getStringExtra("audioUrl") ?: return START_NOT_STICKY
        val audiobookId = intent.getIntExtra("audiobookId", -1)
        val chapterIndex = intent.getIntExtra("chapterIndex", 0)


        // Initialize ExoPlayer if it's null
        if (exoPlayer == null) {
            Log.d("AudioPlayerServiceAudioPlayerService", "Initializing ExoPlayer")

            exoPlayer = ExoPlayer.Builder(this).build()
        }
        // Promote the service to a foreground service
        val notification = createNotification(audiobookId, chapterIndex)
        startForeground(NOTIFICATION_ID, notification)

        requestAudioFocus()

        // Prepare and play the audio
        val mediaItem = MediaItem.fromUri(audioUrl)
        exoPlayer?.setMediaItem(mediaItem)
        exoPlayer?.prepare()

        // Restore playback state
        restorePlaybackState()
        return START_STICKY
    }

    fun getDuration(): Long {
        return exoPlayer?.duration ?: 0L
    }


    override fun onBind(p0: Intent?): IBinder {
        return binder
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun requestAudioFocus() {
        val focusRequest = AudioFocusRequest.Builder(AudioManager.AUDIOFOCUS_GAIN).apply {
            setOnAudioFocusChangeListener { focusChange ->
                when (focusChange) {
                    AudioManager.AUDIOFOCUS_LOSS -> pause()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT -> pause()
                    AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK -> exoPlayer?.volume = 0.2f
                    AudioManager.AUDIOFOCUS_GAIN -> {
                        exoPlayer?.volume = 1.0f
                        play()
                    }
                }
            }
        }.build()

        val result = audioManager.requestAudioFocus(focusRequest)
        if (result == AudioManager.AUDIOFOCUS_REQUEST_GRANTED) {
            Log.d("AudioPlayerService", "Audio focus granted")
        } else {
            Log.e("AudioPlayerService", "Audio focus request failed")
        }
    }

    private fun createNotification(audiobookId: Int, chapterIndex: Int): Notification {
        val channelId = "audio_player_channel"
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Create a notification channel (required for Android O and above)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                channelId,
                "Audio Player",
                NotificationManager.IMPORTANCE_LOW
            )
            notificationManager.createNotificationChannel(channel)
        }

        // Create an intent to launch the PlayerScreen
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            putExtra("redirect_to_player", true)
            putExtra("audiobookId", audiobookId)
            putExtra("chapterIndex", chapterIndex)
           // putExtra("currentPosition", exoPlayer?.currentPosition ?: 0L)
        }

        val pendingIntent = PendingIntent.getActivity(
            this,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )


        // Build the notification
        return NotificationCompat.Builder(this, channelId)
            .setContentTitle("Audio Player")
            .setContentText("Playing audio...")
            .setSmallIcon(R.drawable.coverart_placeholder)
            .setContentIntent(pendingIntent) // Set the PendingIntent// Replace with your icon
            .build()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onDestroy() {


        // Save playback state before destroying the service
        savePlaybackState(exoPlayer?.isPlaying ?: false, exoPlayer?.currentPosition ?: 0L)

        // Abandon audio focus
        audioFocusRequest?.let {
            audioManager.abandonAudioFocusRequest(it)
        }
        exoPlayer?.release()

        exoPlayer = null
        // Stop the service
        stopSelf()
        super.onDestroy()
    }

    fun play() {
        exoPlayer?.play()
        savePlaybackState(true, exoPlayer?.currentPosition ?: 0L)
    }

    fun pause() {
        exoPlayer?.pause()
        savePlaybackState(false, exoPlayer?.currentPosition ?: 0L)

    }

    fun seekBack() {
        Log.d("AudioPlayerService", "Seeking back...")
        exoPlayer?.seekBack()
        savePlaybackState(exoPlayer?.isPlaying ?: false, exoPlayer?.currentPosition ?: 0L)
    }

    fun seekForward() {
        Log.d("AudioPlayerService", "Seeking forward...")
        exoPlayer?.seekForward()
        savePlaybackState(exoPlayer?.isPlaying ?: false, exoPlayer?.currentPosition ?: 0L)
    }

    fun seekTo(position: Long) {
        Log.d("AudioPlayerService", "Seeking to position: $position")
        Log.d("AudioPlayerService", "exoplayer is playing: ${exoPlayer?.isPlaying}")
        exoPlayer?.seekTo(position)
        savePlaybackState(exoPlayer?.isPlaying ?: true, position)
    }

    fun getCurrentPosition(): Long {
        return exoPlayer?.currentPosition ?: 0L
    }

    fun isPlaying(): Boolean {
        return exoPlayer?.isPlaying ?: true
    }

    fun setPlaybackSpeed(selectedSpeed: Float) {
        exoPlayer?.setPlaybackSpeed(selectedSpeed)
    }

    companion object {
        private const val NOTIFICATION_ID = 1

        @RequiresApi(Build.VERSION_CODES.O)
        fun startService(context: Context, audioUrl: String, audiobookId: Int, chapterIndex: Int) {
            val intent = Intent(context, AudioPlayerService::class.java).apply {
                putExtra("audioUrl", audioUrl)
                putExtra("audiobookId", audiobookId)
                putExtra("chapterIndex", chapterIndex)
            }
            context.startForegroundService(intent)
        }

        fun stopService(context: Context) {
            val intent = Intent(context, AudioPlayerService::class.java)
            context.stopService(intent)
        }
    }

    private fun savePlaybackState(isPlaying: Boolean, position: Long) {
        val sharedPreferences = getSharedPreferences("audio_player", MODE_PRIVATE)
        sharedPreferences.edit().apply {
            putBoolean("is_playing", isPlaying)
            putLong("current_position", position)
            apply()
        }
        Log.d(
            "AudioPlayerService",
            "Playback state saved: isPlaying=$isPlaying, position=$position"
        )
    }

    private fun restorePlaybackState() {
        val sharedPreferences = getSharedPreferences("audio_player", MODE_PRIVATE)
        val isPlaying = sharedPreferences.getBoolean("is_playing", false)
        val position = sharedPreferences.getLong("current_position", 0L)

        exoPlayer?.seekTo(position)
        if (isPlaying) {
            exoPlayer?.play()
        } else {
            exoPlayer?.pause()
        }

        Log.d(
            "AudioPlayerService",
            "Playback state restored: isPlaying=$isPlaying, position=$position"
        )
    }

    override fun onTaskRemoved(rootIntent: Intent?) {
        savePlaybackState(exoPlayer?.isPlaying ?: false, exoPlayer?.currentPosition ?: 0L)
        Log.d("AudioPlayerService", "App closed completely, saved playback state")
        super.onTaskRemoved(rootIntent)
        // Save playback state when the app is closed completely

    }

}