package com.example.muzpleer.ui.player

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.model.PlaylistRepository
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.util.ProgressState
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PlayerViewModel(
    private val musicServiceHandler: MusicServiceHandler,
    private val repository: PlaylistRepository  // Инжектированный репозиторий
) : ViewModel() {

    private val _isInitialized = MutableStateFlow(false)
    val isInitialized: StateFlow<Boolean> = _isInitialized

    private val _currentMediaItemApp = MutableStateFlow<MusicTrack?>(null)
    val currentMediaItemApp: StateFlow<MusicTrack?> = _currentMediaItemApp

    private val _playbackState = MutableStateFlow<PlaybackState>(PlaybackState.IDLE)
    val playbackState: StateFlow<PlaybackState> = _playbackState

    private val _progress = MutableStateFlow(ProgressState(0L, 0L))
    val progress: StateFlow<ProgressState> = _progress

    private val _errorMessage = MutableStateFlow<String?>(null)
    val errorMessage: StateFlow<String?> = _errorMessage

    private var progressUpdateJob: Job? = null

    private val _playlist = MutableStateFlow<List<MusicTrack>>(emptyList()) ///
    val playlist:StateFlow<List<MusicTrack>> = _playlist
    private val _currentPosition = MutableStateFlow(0)

    init {
        musicServiceHandler.setTrackEndListener { itemMusicTrack->
            //Log.d(TAG, "@@@!!!PlayerViewModel init : cover = ${itemMusicTrack.cover } ")
            _currentMediaItemApp.value = itemMusicTrack
        }

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
    }

    fun setCurrentMediaItem(track: MusicTrack){
        _currentMediaItemApp.value = track
        Log.d(TAG, "@@@PlayerViewModel setCurrentMediaItem : cover = ${track.cover } ")
    }

    fun setPlayList(playlist:List<MusicTrack>){
        _playlist.value = playlist  //запоминаем плейлист
        musicServiceHandler.setPlaylist(playlist) // Передаем плейлист в сервис
    }

    fun playMedia(musicTrack: MusicTrack) {
        val index = if (musicTrack.isLocal){
            Log.d(TAG, "@@@PlayerViewModel playMedia Local: title = ${musicTrack.title } ")
            _playlist.value.indexOfFirst { it.mediaUri == musicTrack.mediaUri  }
        }else{
            _playlist.value.indexOfFirst {
                Log.d(TAG, "@@@PlayerViewModel playMedia notLocal: title=${musicTrack.title } ")
                it.resourceId == musicTrack.resourceId  }
        }

        Log.d(TAG, "@@@PlayerViewModel playMedia index = $index ")
        if (index != -1) {
            // Обновляем позицию в плейлисте
            _currentPosition.value = index
            //фиксируем MediaItem
            //_currentMediaItemApp.value = musicTrack
            musicServiceHandler.playMedia(index)
        }
        else {
            _errorMessage.value = "@@@ Track not found in playlist"
        }
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

    fun seekRelative(offsetMs: Long) {
        viewModelScope.launch {
            musicServiceHandler.getCurrentPosition()?.let { currentPos ->
                val newPosition = (currentPos + offsetMs).coerceAtLeast(0)
                musicServiceHandler.seekTo(newPosition)
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
                        currentMediaItemApp.value?.let {
                            musicServiceHandler.play()
                        } ?: run {
                            _errorMessage.value = "No media item selected"
                        }
                    }

                    PlaybackState.IDLE,
                    PlaybackState.ENDED -> {
                        currentMediaItemApp.value?.let { musicTrack->

                            val index = if (musicTrack.isLocal){
                                _playlist.value.indexOfFirst { it.mediaUri == musicTrack.mediaUri  }
                            }else{
                                _playlist.value.indexOfFirst { it.resourceId == musicTrack.resourceId  }
                            }

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
        //val currentIndex = _playlist.value.indexOfFirst { it.music == _currentMediaItemApp.value?.music }

        val currentIndex = if (_currentMediaItemApp.value?.isLocal == true){
            _playlist.value.indexOfFirst { it.mediaUri == _currentMediaItemApp.value?.mediaUri  }
        }else{
            _playlist.value.indexOfFirst { it.resourceId == _currentMediaItemApp.value?.resourceId  }
        }

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