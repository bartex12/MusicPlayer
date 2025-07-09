package com.example.muzpleer.model

data class Playlist(
    val id: String,
    val name: String,
    val tracks: List<MediaItem>,
    val createdAt: Long = System.currentTimeMillis()
)
