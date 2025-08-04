package com.example.muzpleer.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.example.muzpleer.home.HomeStorage
import com.example.muzpleer.home.HomeStorageImpl
import com.example.muzpleer.home.HomeViewModel
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.MyRepository
import com.example.muzpleer.scaner.MediaScanner
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.ui.local.frags.AlbumViewModel
import com.example.muzpleer.ui.local.frags.ArtistsViewModel
import com.example.muzpleer.ui.local.frags.FolderViewModel
import com.example.muzpleer.ui.local.frags.LocalViewModel
import com.example.muzpleer.ui.player.PlayerViewModel
import com.example.muzpleer.ui.tabs.base.BaseStorage
import com.example.muzpleer.ui.tabs.base.BaseStorageImpl
import com.example.muzpleer.ui.tabs.base.BaseViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single <BaseStorage>{ BaseStorageImpl(get())}
    single <HomeStorage>{ HomeStorageImpl(get())}
    single { MediaScanner(get()) }
    single { MyRepository()}

    single { provideExoPlayer(get()) }
    factory {
        MusicServiceHandler(
            context = androidContext(),
            callback = get() // Получаем реализацию колбэка из контейнера
        )
    }
    viewModel { BaseViewModel(get())}
    viewModel { HomeViewModel(get()) }
    viewModel { LocalViewModel(get()) }
    viewModel { AlbumViewModel(get()) }
    viewModel { ArtistsViewModel(get()) }
    viewModel { FolderViewModel(get()) }
    viewModel {
        PlayerViewModel(
            application = get(),
            playerHandler = get() // Получаем MusicServiceHandler из контейнера)
        )
    }
    // Регистрируем заглушку для колбэка (реальная реализация будет в ViewModel)
    factory<MusicServiceHandler.PlayerCallback> {
        object : MusicServiceHandler.PlayerCallback {
            override fun onTrackChanged(track: Song) {}
            override fun onPlaybackStateChanged(isPlaying: Boolean) {}
            override fun onPositionChanged(position: Long, duration: Long) {}
            override fun onError(message: String) {}
        }
    }
}

private fun provideExoPlayer(context: Context): ExoPlayer {
    return ExoPlayer.Builder(context).build()
}