package com.example.muzpleer.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.example.muzpleer.home.HomeStorage
import com.example.muzpleer.home.HomeStorageImpl
import com.example.muzpleer.home.HomeViewModel
import com.example.muzpleer.model.Song
import com.example.muzpleer.model.MyRepository
import com.example.muzpleer.repository.MusicRepository
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import com.example.muzpleer.ui.tabs.base.MyStorage
import com.example.muzpleer.ui.tabs.base.MyStorageImpl
import com.example.muzpleer.ui.tabs.base.MyViewModel
import org.koin.android.ext.koin.androidContext
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {

    single <MyStorage>{ MyStorageImpl(get())}
    single <HomeStorage>{ HomeStorageImpl(get())}
    single { MusicRepository(get()) }
    single { MyRepository()}

    single { provideExoPlayer(get()) }
    factory {
        MusicServiceHandler(
            context = androidContext(),
            callback = get() // Получаем реализацию колбэка из контейнера
        )
    }
    viewModel { SharedViewModel(get(),get())}
    viewModel { MyViewModel(get())}
    viewModel { HomeViewModel(get()) }

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