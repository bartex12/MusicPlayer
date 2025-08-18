package com.example.muzpleer.di

import androidx.room.Room
import com.example.muzpleer.data.AppDatabase
import com.example.muzpleer.data.TrackDao
import org.koin.android.ext.koin.androidContext
import org.koin.dsl.module

val databaseModule = module {

    // Создаём одиночный экземпляр (singleton) базы данных
    single<AppDatabase> {
        Room.databaseBuilder(
            androidContext(),
            AppDatabase::class.java,
            "music_player_db"
        ).build()
    }

    // Создаём одиночный экземпляр DAO, получая его из AppDatabase
    single<TrackDao> {
        get<AppDatabase>().trackDao()
    }
}