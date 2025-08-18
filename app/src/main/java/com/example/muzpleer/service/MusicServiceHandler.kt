package com.example.muzpleer.service

import android.content.Context
import android.util.Log
import androidx.media3.common.MediaItem
import androidx.media3.common.PlaybackException
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.muzpleer.model.Song
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class MusicServiceHandler(
    private val context: Context,
    internal var callback: PlayerCallback
) {
    private var player: ExoPlayer? = null
    internal var playlist: List<Song> = emptyList()
    private var currentIndex = 0
    private var positionUpdateJob: Job? = null

    interface PlayerCallback {
        fun onTrackChanged(track: Song)
        fun onPlaybackStateChanged(isPlaying: Boolean)
        fun onPositionChanged(position: Long, duration: Long)
        fun onError(message: String)
    }

    init {  initializePlayer() }

    private fun initializePlayer() {
        player = ExoPlayer.Builder(context).build().apply {

            addListener(object : Player.Listener {
                override fun onIsPlayingChanged(isPlaying: Boolean) {
                    callback.onPlaybackStateChanged(isPlaying)
                }

                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                        playNext()
                    }
                }

                override fun onPlayerError(error: PlaybackException) {
                    callback.onError("Playback error: ${error.message}")
                }

                override fun onPlaybackStateChanged(playbackState: Int) {
                    when(playbackState){
                        // Автопереключение при окончании трека
                        Player.STATE_ENDED ->{
                            playNext()
                        }
                    }
                }
            })
        }
        startPositionUpdates()
    }

    fun setPlaylist(tracks: List<Song>, startIndex: Int = 0) {
        playlist = tracks
        currentIndex = startIndex
        playTrack(startIndex)
    }

    fun playTrack(index: Int) {
        if (index !in playlist.indices) return

        currentIndex = index

        val track = playlist[index]

        Log.d(TAG, "@@@MusicServiceHandler playTrack: index = $index  track = ${track.title}")

        player?.let { p ->
            val mediaItem =MediaItem.fromUri(track.getContentUri())
            p.setMediaItem(mediaItem)
            p.prepare()
            p.play()
            callback.onTrackChanged(track)
        }
    }

    fun togglePlayPause() {
        player?.let {
            if (it.isPlaying) it.pause() else it.play()
        }
    }

    fun playNext() {
        if (currentIndex < playlist.size - 1) {
            playTrack(currentIndex + 1)
        } else {
            // player?.stop() // после проигрывания последнего трека плейлиста - остановка
            playTrack(0)// после проигрывания последнего трека плейлиста - переход к первому треку
        }
    }

    fun playPrevious() {
        if (currentIndex > 0) {
            playTrack(currentIndex - 1)
        }
    }

    fun seekTo(position: Long) {
        player?.seekTo(position)
    }

    fun getCurrentPosition(): Long = player?.currentPosition ?: 0

//    fun getDuration(): Long = player?.duration ?: 0
//
//    fun isPlaying(): Boolean = player?.isPlaying == true

    private fun startPositionUpdates() {
        positionUpdateJob?.cancel()
        positionUpdateJob = CoroutineScope(Dispatchers.Main).launch {
            while (true) {
                player?.let {
                    callback.onPositionChanged(it.currentPosition, it.duration)
                }
                delay(1000) // Обновляем позицию каждую секунду
            }
        }
    }

    fun release() {
        positionUpdateJob?.cancel()
        player?.release()
        player = null
    }

    companion object{
        const val TAG = "33333"
    }
}

