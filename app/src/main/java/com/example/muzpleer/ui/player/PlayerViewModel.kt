package com.example.muzpleer.ui.player

import android.app.Application
import android.content.Context
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.content.FileProvider
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.service.MusicServiceHandler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlayerViewModel(
    application: Application,
    private val playerHandler: MusicServiceHandler
) : AndroidViewModel(application), MusicServiceHandler.PlayerCallback {

    private val _currentTrack = MutableLiveData<MusicTrack?>()
    val currentTrack: LiveData<MusicTrack?> = _currentTrack

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<Long>(0L)
    val currentPosition: LiveData<Long> = _currentPosition

    private val _duration = MutableLiveData<Long>(0L)
    val duration: LiveData<Long> = _duration

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

   // private var playerHandler: MusicServiceHandler? = null

    init {
        Log.d(TAG, "PlayerViewModel init: playerHandler =$playerHandler ")
        playerHandler.callback = this
    }

    override fun onTrackChanged(track: MusicTrack) {
        _currentTrack.postValue(track)
    }

    override fun onPlaybackStateChanged(isPlaying: Boolean) {
        _isPlaying.postValue(isPlaying)
    }

    override fun onPositionChanged(position: Long, duration: Long) {
        _currentPosition.postValue(position)
        _duration.postValue(duration)
    }

    override fun onError(message: String) {
        _errorMessage.postValue(message)
    }

    fun setPlaylist(playlist: List<MusicTrack>, initialIndex: Int = 0) {
        playerHandler?.setPlaylist(playlist, initialIndex)
    }

    fun playTrack(track: MusicTrack) {
        playerHandler?.let { handler ->
            val index = handler.playlist.indexOfFirst { it.id == track.id }
            if (index != -1) {
                handler.playTrack(index)
            }
        }
    }

    fun togglePlayPause() {
        playerHandler?.togglePlayPause()
    }

    fun playNext() {
        playerHandler?.playNext()
    }

    fun playPrevious() {
        playerHandler?.playPrevious()
    }

    fun seekTo(position: Long) {
        playerHandler?.seekTo(position)
    }

    override fun onCleared() {
        super.onCleared()
        playerHandler?.release()
        Log.d(TAG, "PlayerViewModel onCleared: playerHandler =$playerHandler ")
    }

    fun clearError() {
        _errorMessage.value = null
    }


    companion object{
        const val TAG ="33333"
    }
}