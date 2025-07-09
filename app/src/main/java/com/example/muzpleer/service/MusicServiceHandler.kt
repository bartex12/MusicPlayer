package com.example.muzpleer.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.muzpleer.model.MediaItem
import com.example.muzpleer.model.getPlayList
import com.example.muzpleer.ui.player.PlaybackState
import com.example.muzpleer.util.PlaybackProgress
import kotlin.invoke

class MusicServiceHandler(
    private val context: Context
) {
    companion object{
        const val TAG = "33333"
    }
    private var player: ExoPlayer? = null
    private var mediaSession: MediaSession? = null
    //внешний колбэк для ViewModel
    private var playbackStateListener: ((PlaybackState) -> Unit)? = null

    // Слушатель событий плеера (private, не путать с внешним колбэком playbackStateListener)
    private val playerListener = object : Player.Listener {
        override fun onPlaybackStateChanged(state: Int) {
            updatePlaybackState()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            updatePlaybackState()
        }
    }

    // Устанавливаем внешний колбэк для ViewModel
    fun setPlaybackStateListener(listener: (PlaybackState) -> Unit) {
        this.playbackStateListener = listener
    }

    private fun updatePlaybackState() {
        player?.let { player ->
            val state = when {
                player.isPlaying -> PlaybackState.PLAYING
                player.playbackState == Player.STATE_READY -> PlaybackState.PAUSED
                player.playbackState == Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                player.playbackState == Player.STATE_ENDED -> PlaybackState.ENDED
                else -> PlaybackState.IDLE
            }
            playbackStateListener?.invoke(state)
        }
    }

    fun initializePlayer() {
        player = ExoPlayer.Builder(context)
            .setHandleAudioBecomingNoisy(true) // Автоматическая пауза при отключении наушников
            .build()
            .apply {
                setAudioAttributes(AudioAttributes.DEFAULT, true) // Использование аудиофокуса
                addListener(playerListener)
        }
        mediaSession = MediaSession.Builder(context, player!!)
            .setId(System.currentTimeMillis().toString()) // Уникальный ID
            .build()
    }

//    private fun createPlayerListener() = object : Player.Listener {
//        override fun onPlaybackStateChanged(state: Int) {
//            updatePlaybackState()
//        }
//
//        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
//            updatePlaybackState()
//        }
//
//        private fun updatePlaybackState() {
//            player?.let { player ->
//                val state = when (player.playbackState) {
//                    Player.STATE_BUFFERING -> PlaybackState.BUFFERING
//                    Player.STATE_READY -> if (player.playWhenReady) PlaybackState.PLAYING else PlaybackState.PAUSED
//                    Player.STATE_ENDED -> PlaybackState.ENDED
//                    else -> PlaybackState.IDLE
//                }
//                playbackStateListener?.invoke(state)
//            }
//        }
//    }


    fun playMedia(mediaItem: MediaItem) {
        try {
            player?.apply {
                val uri = "android.resource://${context.packageName}/${mediaItem.music}"
                Log.d(TAG, "MusicServiceHandler playMedia - Preparing media: $uri")

                setMediaItem(androidx.media3.common.MediaItem.fromUri(Uri.parse(uri)))
                prepare()
                play()
            }
        } catch (e: Exception) {
            Log.d(TAG, "Error playing media error = ${e.message}")
        }
    }

    fun play() {
        Log.d(TAG, "MusicServiceHandler play()  ")
        try {
            player?.play()
        } catch (e: Exception) {
            Log.d(TAG, "Error in play() = ${e.message}")
        }
    }

    fun pause() {
        Log.d(TAG, "MusicServiceHandler pause()   ")
        player?.pause()
    }

    fun skipToPrevious() {
        // Implement actual skip logic
        player?.seekToPrevious()
    }

    fun skipToNext() {
        // Implement actual skip logic
        player?.seekToNext()
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun getProgress(): PlaybackProgress? {
        return player?.let {
            PlaybackProgress(
                currentPosition = it.currentPosition,
                duration = it.duration
            )
        }
    }

    fun isPlaying(): Boolean {
        return player?.isPlaying == true
    }

    fun isPaused(): Boolean {
        return player?.isPlaying == false
    }

    fun releasePlayer() {
        player?.let {
            it.removeListener(playerListener)
            it.release()
        }
        mediaSession?.release()
        player = null
        mediaSession = null
        playbackStateListener = null // Очищаем колбэк
        Log.d(TAG, "Player fully released")
    }
}
