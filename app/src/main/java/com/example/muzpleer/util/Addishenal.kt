package com.example.muzpleer.util

fun getTracksCountString(count: Int): String {
    return when {
        count % 100 in 11..14 -> "$count треков"
        count % 10 == 1 -> "$count трек"
        count % 10 in 2..4 -> "$count трека"
        else -> "$count треков"
    }
}

fun getAlbumsCountString(count: Int): String {
    return when {
        count % 100 in 11..14 -> "$count альбомов "
        count % 10 == 1 -> "$count альбом"
        count % 10 in 2..4 -> "$count альбома"
        else -> "$count альбомов"
    }
}