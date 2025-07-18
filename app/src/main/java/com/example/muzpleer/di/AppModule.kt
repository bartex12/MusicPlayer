package com.example.muzpleer.di

import android.content.Context
import androidx.media3.exoplayer.ExoPlayer
import com.example.muzpleer.model.PlaylistRepository
import com.example.muzpleer.scaner.MediaScanner
import com.example.muzpleer.ui.local.LocalMusicViewModel
import com.example.muzpleer.ui.player.PlayerViewModel
import com.example.muzpleer.ui.tracks.TracksViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MediaScanner(get()) }
    single { PlaylistRepository()}
    single { provideExoPlayer(get()) }


    viewModel { TracksViewModel(get()) }
    viewModel { PlayerViewModel(get(), get()) }
    viewModel { LocalMusicViewModel(get()) }
}

private fun provideExoPlayer(context: Context): ExoPlayer {
    return ExoPlayer.Builder(context).build()
}