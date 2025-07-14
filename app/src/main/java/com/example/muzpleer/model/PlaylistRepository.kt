package com.example.muzpleer.model

import com.example.muzpleer.R

class PlaylistRepository {
    fun getPlaylist(): List<MediaItemApp> {
        return listOf(
            MediaItemApp(
                artist = "Riffusion",
                title = "Бездомный кот",
                cover =R.drawable.cat,
                music = R.raw.cat),
            MediaItemApp(
                artist = "Riffusion",
                title = "Дверь",
                cover =R.drawable.door,
                music = R.raw.door),
            MediaItemApp(
                artist = "Riffusion",
                title = "Капли на стекле",
                cover =R.drawable.drops,
                music = R.raw.drops),
            MediaItemApp(
                artist = "Riffusion",
                title = "Нефертити",
                cover =R.drawable.nefertiti,
                music = R.raw.nefertiti),
            MediaItemApp(
                artist = "Riffusion",
                title = "Одуванчик",
                cover =R.drawable.blowball,
                music = R.raw.blowball),
            MediaItemApp(
                artist = "Riffusion",
                title = "И снова пишу",
                cover =R.drawable.and_write_again,
                music = R.raw.and_write_again),
            MediaItemApp(
                artist = "Riffusion",
                title = "Космо",
                cover =R.drawable.cosmo,
                music = R.raw.cosmo),
            MediaItemApp(
                artist = "Riffusion",
                title = "Снежинки",
                cover =R.drawable.snow,
                music = R.raw.snow),
            MediaItemApp(
                artist = "Riffusion",
                title = "Три поросёнка",
                cover =R.drawable.three_pigs1,
                music = R.raw.three_pigs1),
            MediaItemApp(
                artist = "Riffusion",
                title = "Три пути",
                cover =R.drawable.three_way,
                music = R.raw.three_way),
            MediaItemApp(
                artist = "Suno",
                title = "Эльбрусская кругосветка",
                cover =R.drawable.elbrus,
                music = R.raw.elbrus),
            MediaItemApp(
                artist = "Suno",
                title = "Свиная морда",
                cover =R.drawable.fate,
                music = R.raw.fate),
            MediaItemApp(
                artist = "Suno",
                title = "Жизнь его проиграла в карты",
                cover =R.drawable.live_losted,
                music = R.raw.live_losted),
            MediaItemApp(
                artist = "Suno",
                title = "Она сидела у окна",
                cover =R.drawable.near_window,
                music = R.raw.near_window),
            MediaItemApp(
                artist = "Suno",
                title = "Вся наша жизнь состоит из ошибок",
                cover =R.drawable.mistakes,
                music = R.raw.mistakes),
            MediaItemApp(
                artist = "Suno",
                title = "Песня о буревестнике",
                cover =R.drawable.petrel,
                music = R.raw.petrel),
            MediaItemApp(
                artist = "Suno",
                title = "Рощино",
                cover =R.drawable.raivola,
                music = R.raw.raivola),
            MediaItemApp(
                artist = "Suno",
                title = "Когда тебя увидел в первый раз",
                cover =R.drawable.when_first,
                music = R.raw.when_first),
            MediaItemApp(
                artist = "Riffusion",
                title = "Роза1",
                cover =R.drawable.rose1,
                music = R.raw.rose1),
            MediaItemApp(
                artist = "Riffusion",
                title = "Роза2",
                cover =R.drawable.rose2,
                music = R.raw.rose2),
            MediaItemApp(
                artist = "Suno",
                title = "Пустые окна, брошеный отель",
                cover =R.drawable.windows,
                music = R.raw.windows)
        )
    }
}