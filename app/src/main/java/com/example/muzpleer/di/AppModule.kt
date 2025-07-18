package com.example.muzpleer.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.example.muzpleer.model.MusicTrack
import com.example.muzpleer.model.PlaylistRepository
import com.example.muzpleer.scaner.MediaScanner
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.ui.local.LocalMusicViewModel
import com.example.muzpleer.ui.player.PlayerViewModel
import com.example.muzpleer.ui.tracks.TracksViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single { MediaScanner(get()) }
    single { PlaylistRepository()}
    single { provideExoPlayer(get()) }
    factory {
        MusicServiceHandler(
            context = androidContext(),
            callback = get() // Получаем реализацию колбэка из контейнера
        )
    }

    viewModel { TracksViewModel(get()) }
    viewModel { LocalMusicViewModel(get()) }
    viewModel {
        PlayerViewModel(
            application = get(),
            playerHandler = get() // Получаем MusicServiceHandler из контейнера)
        )
    }


    // Регистрируем заглушку для колбэка (реальная реализация будет в ViewModel)
    factory<MusicServiceHandler.PlayerCallback> {
        object : MusicServiceHandler.PlayerCallback {
            override fun onTrackChanged(track: MusicTrack) {}
            override fun onPlaybackStateChanged(isPlaying: Boolean) {}
            override fun onPositionChanged(position: Long, duration: Long) {}
            override fun onError(message: String) {}
        }
    }
}

private fun provideExoPlayer(context: Context): ExoPlayer {
    return ExoPlayer.Builder(context).build()
}