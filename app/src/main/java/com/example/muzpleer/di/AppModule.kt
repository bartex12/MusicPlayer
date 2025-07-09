package com.example.muzpleer.di

import com.example.muzpleer.model.PlaylistRepository
import com.example.muzpleer.service.MusicServiceHandler
import com.example.muzpleer.ui.player.PlayerViewModel
import com.example.muzpleer.ui.tracks.TracksViewModel
import org.koin.androidx.viewmodel.dsl.viewModel
import org.koin.dsl.module

val appModule = module {
    single { MusicServiceHandler(get()) }
    single { PlaylistRepository()}

    viewModel { TracksViewModel(get()) }
    viewModel { PlayerViewModel(get(), get()) }
    //viewModel { PlaylistViewModel(get()) }
}
