package com.example.muzpleer.ui.player

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.media3.exoplayer.ExoPlayer

class PlayerViewModelFactory(
    private val application: Application,
    private val exoPlayer: ExoPlayer
) : ViewModelProvider.Factory {

    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(PlayerViewModel::class.java)) {
            return PlayerViewModel(application, exoPlayer) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}