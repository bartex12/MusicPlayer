package com.example.muzpleer.di

import com.example.muzpleer.room.AppDatabase
import com.example.muzpleer.room.dao.AlbumDao
import com.example.muzpleer.room.dao.ArtistDao
import com.example.muzpleer.room.dao.FolderDao
import com.example.muzpleer.room.dao.SongDao
import org.koin.dsl.module

val databaseModule = module {
    // Синглтон базы данных
    single<AppDatabase> {
        AppDatabase.create(get())
    }

    // DAO
    single<SongDao> { get<AppDatabase>().songDao() }
    single<FolderDao> { get<AppDatabase>().folderDao() }
    single<AlbumDao> { get<AppDatabase>().albumDao() }
    single<ArtistDao> { get<AppDatabase>().artistDao() }

    // Репозитории
//    single<SongRepository> { SongRepository(get(), get()) }
//    single<FolderRepository> { FolderRepository(get()) }
//    single<AlbumRepository> { AlbumRepository(get()) }
//    single<ArtistRepository> { ArtistRepository(get()) }

}
