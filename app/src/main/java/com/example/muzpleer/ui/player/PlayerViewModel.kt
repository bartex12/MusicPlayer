package com.example.muzpleer.ui.player

import android.util.Log
import androidx.core.net.toUri
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.muzpleer.model.MusicTrack
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val player: ExoPlayer
) : ViewModel() {


    private val _currentTrack = MutableLiveData<MusicTrack?>()
    val currentTrack: LiveData<MusicTrack?> = _currentTrack

    private val _playlist = MutableLiveData<List<MusicTrack>>(emptyList())
    val playlist: LiveData<List<MusicTrack>> = _playlist

//    private val _currentIndex = MutableLiveData<Int>()
//    val currentIndex: LiveData<Int> = _currentIndex

    var currentIndex = -1


    private val _isPlaying = MutableLiveData<Boolean>()
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<Long>()
    val currentPosition: LiveData<Long> = _currentPosition

    private val _duration = MutableLiveData<Long>()
    val duration: LiveData<Long> = _duration


    init {
        _playlist.value = emptyList()
        _isPlaying.value = false
        _currentPosition.value = 0L
        _duration.value = 0L

        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.postValue (isPlaying)
            }

            override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                // Обновляем текущий трек при переходе
                if (reason == Player.MEDIA_ITEM_TRANSITION_REASON_AUTO) {
                    playNext()
                }
            }

            override fun onPositionDiscontinuity(
                oldPosition: Player.PositionInfo,
                newPosition: Player.PositionInfo,
                reason: Int
            ) {
                _currentPosition.postValue(newPosition.positionMs)
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }
        })
    }

    fun setPlaylist(playlist: List<MusicTrack>, initialIndex: Int) {
        _playlist.value = playlist
        currentIndex = initialIndex
        playTrack(playlist[initialIndex])
    }

    fun playTrack(track: MusicTrack) {
        viewModelScope.launch (Dispatchers.Main) {
            _currentTrack.value = track
            val mediaItem = MediaItem.fromUri(track.mediaUri.toUri())
            Log.d(TAG,"1@!@#PlayerViewModel playTrack ")
            try{
                player.setMediaItem(mediaItem)
                Log.d(TAG,"2@!@#PlayerViewModel playTrack ")
                player.prepare()
                Log.d(TAG,"3@!@#PlayerViewModel playTrack ")
                player.play()
                Log.d(TAG,"4@!@#PlayerViewModel playTrack ")
                _duration.value = player.duration
                Log.d(TAG,"5@!@#PlayerViewModel playTrack ")
            }catch (e: IllegalStateException) {
                // Обработка ошибки, если поток умер
                Log.d(TAG,"@!@#PlayerViewModel playTrack  - Error playing track ${e.message}")
            }
        }
    }

    fun playPrevious() {
        val playlist = _playlist.value?: return

        if (currentIndex > 0 ) {
            currentIndex--
            playTrack(playlist[currentIndex])
        }
    }

    fun playNext() {
        val playlist = _playlist.value ?: return
        if (currentIndex < playlist.size-1) {
            currentIndex++
            playTrack(playlist[currentIndex])
        } else {
            // Достигнут конец плейлиста
            player.stop()
            _isPlaying.postValue(false)
            _currentPosition.postValue(0)
        }
    }

    fun togglePlayPause() {
        if (player.isPlaying) {
            player.pause()
        } else {
            player.play()
        }
    }

    fun seekTo(position: Long) {
        player.seekTo(position)
    }

    override fun onCleared() {
        super.onCleared()
        player.release()
    }

    fun releasePlayer() {
        player.release()
    }

    companion object{
        const val TAG ="33333"
    }
}