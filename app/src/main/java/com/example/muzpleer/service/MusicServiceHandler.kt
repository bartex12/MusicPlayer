package com.example.muzpleer.service

import android.content.Context
import android.util.Log
import androidx.media3.common.AudioAttributes
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.session.MediaSession
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.util.ProgressState
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import androidx.core.net.toUri

//class MusicServiceHandler(
//    private val context: Context
//) {
//    companion object {
//        const val TAG = "33333"
//    }
//
//    private var player: ExoPlayer? = null
//    private var mediaSession: MediaSession? = null
//
//    //внешний колбэк для ViewModel
//    private var playbackStateListener: ((PlaybackState) -> Unit)? = null
//    private var progressUpdateJob: Job? = null
//    private var playlist: List<MusicTrack> = emptyList()
//    private var currentIndex = 0
//    private var currentTrackListener: ((MusicTrack) -> Unit)? = null
//
//    // Устанавливаем плейлист один раз
//    fun setPlaylist(playlist: List<MusicTrack>) {
//        Log.d(TAG, " *+*  MusicServiceHandler setPlaylist: size = ${playlist.size}  ")
//        this.playlist = playlist
//    }
//
//    fun setCurrentTrackListener(listener: (MusicTrack) -> Unit) {
//        this.currentTrackListener = listener
//    }
//
//    // Устанавливаем внешний колбэк для ViewModel
//    fun setPlaybackStateListener(listener: (PlaybackState) -> Unit) {
//        this.playbackStateListener = listener
//    }
//
//    fun startProgressUpdates(callback: (currentPos: Long, duration: Long) -> Unit) {
//        progressUpdateJob?.cancel()
//        progressUpdateJob = CoroutineScope(Dispatchers.Main).launch {
//            while (true) {
//                player?.let {
//                    callback(it.currentPosition, it.duration)
//                }
//                delay(500) // Обновление каждые 500 мс
//            }
//        }
//    }
//
//    fun stopProgressUpdates() {
//        progressUpdateJob?.cancel()
//    }
//
//    // Слушатель событий плеера (private, не путать с внешним колбэком playbackStateListener)
//    private val playerListener = object : Player.Listener {
//        //Вызывается при переходе воспроизведения к медиа-элементу или начале повтора медиа-элемента
//        // в соответствии с текущим режимом повтора
//        override fun onMediaItemTransition(
//            mediaItem:MediaItem?,
//            reason: Int
//        ) {
//            Log.d(TAG, "+++MusicServiceHandler onMediaItemTransition mediaItem = $mediaItem}" )
//
//            when (reason) {
//                //Воспроизведение автоматически переключилось на следующий медиафайл...
//                Player.MEDIA_ITEM_TRANSITION_REASON_AUTO -> {
//                    //playMedia3(mediaItem)
//                    Log.d(TAG, "MusicServiceHandler onMediaItemTransition mediaItem = $mediaItem}" )
////                    currentIndex = (currentIndex + 1) % playlist.size
////                    playbackStateListener?.invoke(PlaybackState.PLAYING)
//                }
//            }
//        }
//
//        //Вызывается при изменении значения, возвращаемого функцией getPlaybackState().
//        override fun onPlaybackStateChanged(state: Int) {
//            //playbackStateListener?.invoke(state )
//            Log.d(TAG, " %%% Сработал onPlaybackStateChanged:  ")
//            updatePlaybackState()
//
//        }
//
//        //Вызывается при изменении значения, возвращаемого функцией getPlayWhenReady().
//        override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
//            Log.d(TAG, " %%% Сработал onPlayWhenReadyChanged:  ")
//            //updatePlaybackState()
//        }
//    }
//
//    private fun updatePlaybackState() {
//        player?.let { player ->
//            val playbackState = when {
//                player.isPlaying -> PlaybackState.PLAYING
//                player.playbackState == Player.STATE_READY -> {
//                    if (player.isPlaying == true) PlaybackState.PLAYING
//                    else PlaybackState.PAUSED
//                }
//                player.playbackState == Player.STATE_BUFFERING -> PlaybackState.BUFFERING
//                player.playbackState == Player.STATE_ENDED -> {
//                    PlaybackState.ENDED
////                    if (playlist.isNotEmpty())
////                        playNext()  // Автопереключение при окончании трека
////                    PlaybackState.PLAYING
//                }
//                else -> PlaybackState.IDLE
//            }
//            playbackStateListener?.invoke(playbackState)
//        }
//    }
//
//    fun playNext() {
//        val nextIndex = (currentIndex + 1) % playlist.size
//        currentIndex = nextIndex
//        playMedia(nextIndex)
//    }
//
//    fun initializePlayer() {
//        player = ExoPlayer.Builder(context)
//            .setHandleAudioBecomingNoisy(true) // Автоматическая пауза при отключении наушников
//            .build()
//            .apply {
//                setAudioAttributes(AudioAttributes.DEFAULT, true) // Использование аудиофокуса
//                addListener(playerListener) //регистрируем слушатель событий
//            }
//        mediaSession = MediaSession.Builder(context, player!!)
//            .setId(System.currentTimeMillis().toString()) // Уникальный ID
//            .build()
//    }
//
//    fun playMedia(index: Int) {
//        player?.release()
//        player = ExoPlayer.Builder(context).build()
//        if (index !in playlist.indices) return
//        //currentIndex = index
//        val track = playlist[index]
//
//        val newMediaItem = if (track.isLocal) {
//            // Для локальных файлов
//            MediaItem.fromUri(track.mediaUri.toUri())
//        } else {
//            // Для ресурсов приложения
//            track.resourceId?.let { resId ->
//                val uri = "android.resource://${context.packageName}/$resId"
//                MediaItem.fromUri(uri.toUri())
//            } ?: throw IllegalArgumentException("Resource ID is null")
//        }
//        Log.d(TAG, "MusicServiceHandler playMedia newMediaItem = $newMediaItem ")
//        player?.apply {
//            setMediaItem(newMediaItem)
//        }
//
//        player?.prepare() // начать загрузку мультимедиа и получить необходимые ресурсы.
//        Log.d(TAG, "1-MusicServiceHandler playMedia state =  = ${player?.playbackState} ")
//        player?.play()
//        Log.d(TAG, "2-MusicServiceHandler playMedia state =  = ${player?.playbackState} ")
//    }
//
//    //вызывается из ViewModel
//    fun playMedia2(musicTrack: MusicTrack) {
//
//        val mediaItem: MediaItem = if (musicTrack.isLocal) {
//            MediaItem.fromUri(musicTrack.getContentUri())
//        } else {
//            val uri = "android.resource://${context.packageName}/${musicTrack.resourceId}"
//            MediaItem.fromUri(uri.toUri())
//        }
//        Log.d(TAG,"!!//!!MusicServiceHandler playMedia2 mediaItem = $mediaItem  id = ${musicTrack.id}")
//        player?.setMediaItem(mediaItem)
//
//        //обновляем текущий индекс
//        currentIndex = getTruckIndex(musicTrack, playlist)
//        Log.d(TAG,"!!//!!MusicServiceHandler playMedia2 currentIndex = $currentIndex ")
//
////        //todo сомнительно, что это нужно
////        val newMusicTrack = mediaItemToMusicTrack(musicTrack, playlist)
////        currentTrackListener?.invoke(newMusicTrack)
//
//        player?.prepare()
//        player?.play()
//    }
//
//    fun mediaItemToMusicTrack(musicTrack:MusicTrack, playlist:List<MusicTrack>): MusicTrack{
//        val index = getTruckIndex(musicTrack, playlist)
//
//        return MusicTrack(
//            id = playlist[index].id,
//            title = playlist[index].title,
//            artist = playlist[index].artist,
//            duration = playlist[index].duration,
//            mediaUri = playlist[index].mediaUri,// музыка для треков из телефона
//            isLocal = playlist[index].isLocal,
//            artworkUri = playlist[index].artworkUri,  //обложка для треков из телефона
//            album = playlist[index].album,
//            cover = playlist[index].cover, //обложка для треков из ресурсов приложения
//            resourceId = playlist[index].resourceId// музыка для треков из ресурсов приложения
//        )
//    }
//
//    fun getTruckIndex(musicTrack:MusicTrack, playlist:List<MusicTrack>):Int{
//        return if (musicTrack.isLocal){
//            Log.d(TAG, "MusicServiceHandler getTruckIndex Local: title = ${musicTrack.title } ")
//            playlist.indexOfFirst { it.mediaUri == musicTrack.mediaUri  }
//        }else{
//            playlist.indexOfFirst {
//                Log.d(TAG, "MusicServiceHandler getTruckIndex notLocal: title=${musicTrack.title } ")
//                it.resourceId == musicTrack.resourceId  }
//        }
//    }
//
//    fun play() {
//        Log.d(TAG, "MusicServiceHandler play()  ")
//        try {
//            player?.play()
//        } catch (e: Exception) {
//            Log.d(TAG, "Error in play() = ${e.message}")
//        }
//    }
//
//    fun pause() {
//        Log.d(TAG, "MusicServiceHandler pause()   ")
//        player?.pause()
//    }
//
//    fun skipToPrevious() {
//        // Implement actual skip logic
//        player?.seekToPrevious()
//    }
//
//    fun skipToNext() {
//        // Implement actual skip logic
//        player?.seekToNext()
//    }
//
//    fun getCurrentPosition(): Long? {
//        return player?.currentPosition
//    }
//
//    fun seekTo(position: Long) {
//        player?.seekTo(position)
//    }
//
//    fun getProgress(): ProgressState? {
//        return player?.let {
//            ProgressState(
//                currentPosition = it.currentPosition,
//                duration = it.duration
//            )
//        }
//    }
//
//    fun isPlaying(): Boolean {
//        return player?.isPlaying == true
//    }
//
//    fun isPaused(): Boolean {
//        return player?.isPlaying == false
//    }
//
//    fun releasePlayer() {
//        player?.let {
//            it.removeListener(playerListener)
//            it.release()
//        }
//        mediaSession?.release()
//        player = null
//        mediaSession = null
//        playbackStateListener = null // Очищаем колбэк
//        Log.d(TAG, "Player fully released")
//    }
//}
