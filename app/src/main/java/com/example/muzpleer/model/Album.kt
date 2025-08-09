package com.example.muzpleer.model

import android.net.Uri

data class Album(
    val id: String,
    val title: String,
    val artist: String,       // Основной исполнитель или "Various Artists"
    val artists: List<String> =  emptyList(), // Все исполнители в альбоме
    val artworkUri: Uri? = null,
    val albumId: Long, // Добавляем поле для albumId
    val songs: List<Song> = emptyList()
)