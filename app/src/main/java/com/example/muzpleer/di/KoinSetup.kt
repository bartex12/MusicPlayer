package com.example.muzpleer.di

import android.app.Application
import org.koin.android.ext.koin.androidContext
import org.koin.core.context.GlobalContext.startKoin

class MusicApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidContext(this@MusicApplication)
            modules(listOf(appModule, databaseModule)) // Добавляем databaseModule
        }
    }
}
