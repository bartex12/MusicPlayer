package com.example.muzpleer.data

import android.net.Uri
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.example.muzpleer.model.Song
import java.io.File
import kotlin.Long

@Entity(tableName = "tracks_table")
data class TrackEntity(
    @PrimaryKey
    val id: Long,
    val trackName: String,
    val artistName: String,
    val duration: Long,
    val filePath: String,
    val album: String? = null,
    val albumId: Long,
    val folderPath: String // Добавляем путь к папке для удобной группировки
)

// Функция-маппер для преобразования доменной модели в сущность БД
fun Song.toEntity(): TrackEntity {
    val parentFolder = File(this.mediaUri).parent ?: "Unknown"
    return TrackEntity(
        id = this.id,
        trackName = this.title,
        artistName = this.artist,
        duration = this.duration,
        filePath = this.mediaUri,
        album = this.album,
        albumId = this.albumId,
        folderPath = parentFolder
    )
}

// Функция-маппер для обратного преобразования
fun TrackEntity.toDomain(): Song {
    return Song(
        id = this.id,
        title = this.trackName,
        artist = this.artistName,
        duration = this.duration,
        mediaUri = this.filePath,
        album = this.album,
        albumId = this.albumId,
        folderPath = this.folderPath
    )
}