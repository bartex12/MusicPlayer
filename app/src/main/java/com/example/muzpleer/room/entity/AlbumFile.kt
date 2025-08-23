package com.example.muzpleer.room.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "albums",
    foreignKeys = [
        ForeignKey(
            entity = SongFile::class,
            parentColumns = ["mediaStoreId"],
            childColumns = ["mediaStoreId"],
            onDelete = ForeignKey.CASCADE,
            onUpdate = ForeignKey.CASCADE
        )
    ],
    indices = [Index(value = ["mediaStoreId"], unique = true)]
)
data class AlbumFile(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val mediaStoreId: Long, // Связь с MediaFile
    val title: String,
    val artist: String,
    val allArtists: String, // JSON или разделенный список
    val songCount: Int,
    val coverPath: String?,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long = System.currentTimeMillis()
)