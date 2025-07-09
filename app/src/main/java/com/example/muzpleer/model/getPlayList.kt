package com.example.muzpleer.model

import com.example.muzpleer.R

fun  getPlayList():List<MediaItemApp> {
    return listOf(
        MediaItemApp(
            artist = "Queen",
            title = "Bohemian rhapsody",
            cover =R.drawable.bohemian,
            music = R.raw.bohemian),
        MediaItemApp(
            artist = "The Beatles",
            title = "Let it be",
            cover =R.drawable.let_it_be,
            music = R.raw.let_it_be),
        MediaItemApp(
            artist = "ABBA",
            title = "Gimme, gimme",
            cover =R.drawable.gimme,
            music = R.raw.gimme)
    )
}