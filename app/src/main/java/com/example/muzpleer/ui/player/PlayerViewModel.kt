package com.example.muzpleer.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzpleer.model.MediaItem
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.util.PlaybackProgress
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val musicServiceHandler: MusicServiceHandler
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _progress = MutableStateFlow(PlaybackProgress(0L, 0L))
    val progress: StateFlow<PlaybackProgress> = _progress.asStateFlow()

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var progressUpdateJob: Job? = null

    init {
        musicServiceHandler.setPlaybackStateListener { state ->
            _playbackState.value = state
        }
        initializePlayer()
    }

    private fun initializePlayer() {
        viewModelScope.launch {
            try {
                musicServiceHandler.initializePlayer()
                _isInitialized.value = true
                Log.d(TAG, "PlayerViewModel initializePlayer -Player initialized successfully")
            } catch (e: Exception) {
                _errorMessage.value = "Failed to initialize player: ${e.message}"
                Log.d(TAG, "PlayerViewModel initializePlayer - Player initialization error = ${e.message}")
            }
        }
    }

    fun setMediaItem(mediaItem: MediaItem?) {
        mediaItem?.let { item ->
            viewModelScope.launch {
                try {
                    _currentMediaItem.value = item
                    musicServiceHandler.playMedia(item)
                    _errorMessage.value = null
                    startProgressUpdates()
                } catch (e: Exception) {
                    Log.d(TAG, "PlayerViewModel setMediaItem Error setting media item")
                    _errorMessage.value = "Failed to play: ${e.localizedMessage}"
                }
            }
        }
    }

    fun togglePlayPause() {
        viewModelScope.launch {
            try {
                if (!_isInitialized.value) {
                    _errorMessage.value = "Player is not ready yet"
                    return@launch
                }

                when (_playbackState.value) {
                    PlaybackState.PLAYING -> {
                        musicServiceHandler.pause()
                        // Не нужно вручную обновлять _playbackState -
                        // это сделает listener в MusicServiceHandler
                    }

                    PlaybackState.PAUSED -> {
                        currentMediaItem.value?.let {
                            musicServiceHandler.play()
                        } ?: run {
                            _errorMessage.value = "No media item selected"
                        }
                    }

                    PlaybackState.IDLE,
                    PlaybackState.ENDED -> {
                        currentMediaItem.value?.let {
                            musicServiceHandler.playMedia(it)
                        } ?: run {
                            _errorMessage.value = "No media item selected"
                        }
                    }

                    PlaybackState.BUFFERING -> {
                        // Обработка состояния загрузки (можно добавить ожидание)
                        _errorMessage.value = "Buffering in progress"
                    }
                }
            } catch (e: Exception) {
                _errorMessage.value = "Playback error: ${e.localizedMessage}"
                Log.d(TAG, "PlayerViewModel togglePlayPause Playback error = ${e.localizedMessage}")
            }
        }
    }

    fun skipToPrevious() {
        // Implement actual skip logic based on playlist
        musicServiceHandler.skipToPrevious()
        updatePlaybackState()
    }

    fun skipToNext() {
        // Implement actual skip logic based on playlist
        musicServiceHandler.skipToNext()
        updatePlaybackState()
    }

    fun seekTo(position: Long) {
        musicServiceHandler.seekTo(position)
        updateProgressImmediately()
    }

    private fun startProgressUpdates() {
        progressUpdateJob?.cancel()
        progressUpdateJob = viewModelScope.launch {
            while (true) {
                updateProgressImmediately()
                delay(PROGRESS_UPDATE_INTERVAL)
            }
        }
    }

    private fun updateProgressImmediately() {
        musicServiceHandler.getProgress()?.let {
            _progress.value = PlaybackProgress(
                currentPosition = it.currentPosition,
                duration = it.duration
            )
        }
    }

    private fun updatePlaybackState() {
        _playbackState.value = when {
            musicServiceHandler.isPlaying() -> PlaybackState.PLAYING
            musicServiceHandler.isPaused() -> PlaybackState.PAUSED
            else -> PlaybackState.IDLE
        }
        Log.d(TAG, "PlayerViewModel updatePlaybackState - ${playbackState.value}")
    }

    override fun onCleared() {
        super.onCleared()
        progressUpdateJob?.cancel()
        musicServiceHandler.releasePlayer()
        Log.d(TAG, "Player released from ViewModel")
    }

    companion object {
        private const val PROGRESS_UPDATE_INTERVAL = 500L // ms
        const val TAG = "333333"
    }
}

enum class PlaybackState {
    IDLE,         // Плеер не инициализирован
    BUFFERING,    // Идет загрузка
    PLAYING,      // Воспроизведение
    PAUSED,       // На паузе
    ENDED         // Трек завершен
}