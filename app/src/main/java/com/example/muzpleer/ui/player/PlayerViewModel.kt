package com.example.muzpleer.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzpleer.model.MediaItem
import com.example.muzpleer.model.PlaylistRepository
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.util.ProgressState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val musicServiceHandler: MusicServiceHandler,
    private val repository: PlaylistRepository  // Инжектированный репозиторий
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val _currentMediaItem = MutableStateFlow<MediaItem?>(null)
    val currentMediaItem: StateFlow<MediaItem?> = _currentMediaItem

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _progress = MutableStateFlow(ProgressState(0L, 0L))
    val progress: StateFlow<ProgressState> = _progress

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var progressUpdateJob: Job? = null

    private val _playlist = MutableStateFlow<List<MediaItem>>(emptyList()) ///
    private val _currentPosition = MutableStateFlow(0)
    private val _currentIndex = MutableStateFlow(0) ///






    init {
        musicServiceHandler.setPlaybackStateListener { state ->
            when (state) {
                PlaybackState.ENDED -> {
                    viewModelScope.launch {
                        delay(300) // Небольшая задержка перед переходом
                        musicServiceHandler.playNext()
                    }
                }
                else -> _playbackState.value = state
            }
        }
        musicServiceHandler.startProgressUpdates { currentPos, duration ->
            _progress.value = ProgressState(currentPos, duration)
        }
        initializePlayer()
        loadPlaylist() // Загружаем плейлист при создании ViewModel
    }

    private fun loadPlaylist() {
        viewModelScope.launch {
            val playlist = repository.getPlaylist()
            _playlist.value = playlist
            musicServiceHandler.setPlaylist(playlist) // Передаем плейлист в сервис
        }
    }

    fun playMedia(mediaItem: MediaItem) {
        val index = _playlist.value.indexOfFirst { it.music == mediaItem.music  }
        if (index != -1) {
            _currentIndex.value = index
            _currentPosition.value = index
            _currentMediaItem.value = mediaItem
            musicServiceHandler.playMedia(index)
        }
        else {
            _errorMessage.value = "Track not found in playlist"
        }
        // Обновляем позицию в плейлисте
        _currentPosition.value = _playlist.value.indexOfFirst { it.music == mediaItem.music }
            .takeIf { it != -1 } ?: _currentPosition.value
        // Обновляем состояние
        _playbackState.value = PlaybackState.PLAYING
    }

    fun playMedia(index: Int) {
        musicServiceHandler.playMedia(index)
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
                        currentMediaItem.value?.let {mediaItem->
                            val index = _playlist.value.indexOfFirst { it.music == mediaItem.music }
                            if (index != -1) {
                                musicServiceHandler.playMedia(index)
                            } else {
                                _errorMessage.value = "Track not found in playlist"
                            }
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
        val prevPos = (_currentPosition.value - 1).mod(_playlist.value.size)
        _playlist.value.getOrNull(prevPos)?.let { mediaItem ->
            _currentPosition.value = prevPos
            playMedia(mediaItem) // Вызов для предыдущего трека
        }
        updatePlaybackState()
    }

    fun skipToNext() {
        val currentIndex = _playlist.value.indexOfFirst { it.music == _currentMediaItem.value?.music }
        if (currentIndex != -1) {
            val nextIndex = (currentIndex + 1) % _playlist.value.size
            playMedia(nextIndex)
        }


        val nextPos = (_currentPosition.value + 1) % _playlist.value.size
        _playlist.value.getOrNull(nextPos)?.let { mediaItem ->
            _currentPosition.value = nextPos
            playMedia(mediaItem) // Вызов для следующего трека
        }
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
            _progress.value = ProgressState(
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
        musicServiceHandler.stopProgressUpdates()
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