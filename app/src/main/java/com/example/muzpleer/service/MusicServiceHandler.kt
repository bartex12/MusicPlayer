package com.example.muzpleer.service

import android.content.Context
import android.net.Uri
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.muzpleer.model.MediaItem
import com.example.muzpleer.ui.player.PlaybackState
import com.example.muzpleer.util.ProgressState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

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
    private var progressUpdateJob: Job? = null
    private var playlist: List<MediaItem> = emptyList()
    private var currentIndex = 0

//    private var currentPlaylist: List<MediaItem> = emptyList()
//    private var trackChangeListener: ((MediaItem) -> Unit)? = null

    // Устанавливаем плейлист один раз
    fun setPlaylist(playlist: List<MediaItem>) {
        this.playlist = playlist
    }

//    fun setTrackChangeListener(listener: (MediaItem) -> Unit) {
//        this.trackChangeListener = listener
//    }

    fun startProgressUpdates(callback: (currentPos: Long, duration: Long) -> Unit) {
        progressUpdateJob?.cancel()
        progressUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                player?.let {
                    callback(it.currentPosition, it.duration)
                }
                delay(500) // Обновление каждые 500 мс
            }
        }
    }

    fun stopProgressUpdates() {
        progressUpdateJob?.cancel()
    }

    // Устанавливаем внешний колбэк для ViewModel
    fun setPlaybackStateListener(listener: (PlaybackState) -> Unit) {
        this.playbackStateListener = listener
    }

    // Слушатель событий плеера (private, не путать с внешним колбэком playbackStateListener)
    private val playerListener = object : Player.Listener {

        override fun onMediaItemTransition(mediaItem: androidx.media3.common.MediaItem?, reason: Int) {
            when (reason) {
                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO-> {
                    currentIndex = (currentIndex + 1) % playlist.size
                    playbackStateListener?.invoke(PlaybackState.PLAYING)
                }
            }
        }

        override fun onPlaybackStateChanged(state: Int) {
            updatePlaybackState()
        }

        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
            updatePlaybackState()
        }
    }

    private fun updatePlaybackState() {
        player?.let { player ->
            val playbackState  = when {
                player.isPlaying -> PlaybackState.PLAYING
                player.playbackState == Player.STATE_READY -> {
                    if (player?.isPlaying == true) PlaybackState.PLAYING
                    else PlaybackState.PAUSED
                }
                player.playbackState == Player.STATE_BUFFERING -> PlaybackState.BUFFERING
                player.playbackState == Player.STATE_ENDED -> {
                    if (playlist.isNotEmpty())
                        playNext()  // Автопереключение при окончании трека
                        PlaybackState.PLAYING
                }
                else -> PlaybackState.IDLE
            }
            playbackStateListener?.invoke(playbackState )
        }
    }

    fun playNext() {
        val nextIndex = (currentIndex + 1) % playlist.size
        playMedia(nextIndex)
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

    fun playMedia(index: Int) {
        if (index !in playlist.indices) return

        currentIndex = index

        player?.apply{
            setMediaItem(
                androidx.media3.common.MediaItem.fromUri(
                    "android.resource://${context.packageName}/${playlist[index].music}"
                )
            )
        }
        player?.prepare()
        player?.play()
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

    fun getProgress(): ProgressState? {
        return player?.let {
            ProgressState(
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
