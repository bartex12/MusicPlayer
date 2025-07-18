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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

class PlayerViewModel(
    application: Application,
    private val player: ExoPlayer
) : AndroidViewModel(application) {

    private val _currentTrack = MutableLiveData<MusicTrack?>()
    val currentTrack: LiveData<MusicTrack?> = _currentTrack

    private val _playlist = MutableLiveData<List<MusicTrack>>(emptyList())

    var currentIndex = -1

    private val _isPlaying = MutableLiveData<Boolean>(false)
    val isPlaying: LiveData<Boolean> = _isPlaying

    private val _currentPosition = MutableLiveData<Long>(0L)
    val currentPosition: LiveData<Long> = _currentPosition

    private val _duration = MutableLiveData<Long>(0L)
    val duration: LiveData<Long> = _duration

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private val context: Context get() = getApplication<Application>().applicationContext
    private var isPlayerInitialized = false

    init {

        Log.d(TAG, "PlayerViewModel init:  ")
        player.addListener(object : Player.Listener {
            override fun onIsPlayingChanged(isPlaying: Boolean) {
                _isPlaying.value = isPlaying
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
                _currentPosition.value = newPosition.positionMs
            }
            override fun onPlaybackStateChanged(playbackState: Int) {
                if (playbackState == Player.STATE_ENDED) {
                    playNext()
                }
            }
        })
    }

    fun setPlaylist(playlist: List<MusicTrack>, initialIndex: Int) {
        if (initialIndex < 0 || initialIndex >= playlist.size) return

        _playlist.value = playlist
        currentIndex = initialIndex
        playTrack(playlist[initialIndex])
    }

    fun playTrack(track: MusicTrack) {
        if (!isPlayerInitialized) {
            initializePlayer()
        }

        viewModelScope.launch(Dispatchers.Main) {
            try {
                _errorMessage.value = null
                _currentTrack.value = track
                //val mediaItem = MediaItem.fromUri(track.mediaUri.toUri())

                // 1. Попробуем воспроизвести через content URI
                val contentUri = track.getContentUri()
                if (isUriAccessible(contentUri)) {
                    Log.d(TAG, "PlayerViewModel playTrack: через content URI  ")
                    playUri(contentUri)
                    return@launch
                }

                // 2. Попробуем прямой путь через FileProvider
                val fileUri = getFileUri(track)
                if (fileUri != null && isUriAccessible(fileUri)) {
                    Log.d(TAG, "PlayerViewModel playTrack: через FileProvider  ")
                    playUri(fileUri)
                    return@launch
                }

                // 3. Попробуем прямой доступ к файлу (для старых версий Android)
                if (tryDirectFileAccess(track)) {
                    return@launch
                }

                _errorMessage.value = "Не удалось получить доступ к файлу"
            } catch (e: Exception) {
                _errorMessage.value = "Ошибка воспроизведения: ${e.localizedMessage}"
                Log.e(TAG, "Playback error", e)
            }
        }
    }

    private fun initializePlayer() {
        try {
            if (player.playbackState == Player.STATE_IDLE) {
                player.playWhenReady = true
                isPlayerInitialized = true
            }
        } catch (e: Exception) {
            Log.d(TAG, "Player initialization failed ${e.message}")
        }
    }

    private suspend fun playUri(uri: Uri) {
        withContext(Dispatchers.Main.immediate) {
            Log.d(TAG, "PlayerViewModel playUri player.playbackState = ${player.playbackState}  ")
            try {
                // Остановить текущее воспроизведение перед сменой трека
                if (player.playbackState != Player.STATE_IDLE) {
                    player.stop()
                }
                player.setMediaItem(MediaItem.fromUri(uri))
                player.prepare()
                player.play()
                _duration.value = player.duration
            }catch (e: IllegalStateException){
//                // Если плеер в недопустимом состоянии, пересоздаем его
//                recreatePlayer()
//                player.setMediaItem(MediaItem.fromUri(uri))
//                player.prepare()
//                player.play()
            }
        }
    }

    private fun recreatePlayer() {
        player.release()
        val newPlayer = ExoPlayer.Builder(getApplication())
            .setHandleAudioBecomingNoisy(true)
            .build()

//        // Переносим все слушатели на новый плеер
//        player.listeners.forEach { listener ->
//            newPlayer.addListener(listener)
//        }

        // Заменяем ссылку на плеер
        // Note: В реальном приложении вам нужно будет обновить ссылку в зависимостях
        // Это может потребовать изменения архитектуры или использования DI
    }

    private fun isUriAccessible(uri: Uri): Boolean {
        return try {
            context.contentResolver.openInputStream(uri)?.use { true } == true
        } catch (e: Exception) {
            false
        }
    }

    private fun getFileUri(track: MusicTrack): Uri? {
        return try {
            val file = File(track.mediaUri)
            if (!file.exists() || !file.canRead()) return null

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.provider",
                    file
                )
            } else {
                Uri.fromFile(file)
            }
        } catch (e: Exception) {
            null
        }
    }

    private fun tryDirectFileAccess(track: MusicTrack): Boolean {
        Log.d(TAG, "PlayerViewModel tryDirectFileAccess: через прямой доступ к файлу  ")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) return false

        return try {
            val file = File(track.mediaUri)
            if (!file.exists() || !file.canRead()) return false

            val uri = Uri.fromFile(file)
            player.setMediaItem(MediaItem.fromUri(uri))
            player.prepare()
            player.play()
            true
        } catch (e: Exception) {
            false
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
            _isPlaying.value = false
            _currentPosition.value = 0
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

    fun clearError() {
        _errorMessage.value = null
    }

    companion object{
        const val TAG ="33333"
    }
}