package com.example.muzpleer.room.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "artists",
    indices = [Index(value = ["id"], unique = true)] // Уникальность только в таблице artists
)
data class ArtistFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val artistId: Long, //  ID артиста (из MediaFile.artistId или хэш имени)
    val name: String,
    val allAlbumIds: String, // Список ID альбомов через ";"
    val songCount: Int,
    val coverPath: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)
