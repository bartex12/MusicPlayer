package com.example.muzpleer.model

data class Playlist(
    val id: String,
    val name: String,
    val tracks: List<MediaItemApp>,
    val createdAt: Long = System.currentTimeMillis()
)
