package com.example.muzpleer.ui.player

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.service.MusicServiceHandler

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

    init {
        playerHandler.callback = this
        Log.d(TAG, "PlayerViewModel init: playerHandler =$playerHandler ")
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

    fun seekRelative(offsetMs: Long) {
        playerHandler.getCurrentPosition().let { currentPos ->
            val newPosition = (currentPos + offsetMs).coerceAtLeast(0)
            playerHandler.seekTo(newPosition)
        }
    }


    fun clearError() {
        _errorMessage.value = null
    }


    companion object{
        const val TAG ="33333"
    }
}