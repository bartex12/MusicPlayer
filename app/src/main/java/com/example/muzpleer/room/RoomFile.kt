package com.example.muzpleer.room

import androidx.room.Entity
import androidx.room.PrimaryKey


/*будем использовать отдельный класс RoomFile для работы с базой, чтобы не
 вносить изменений в существующие сущности во избежание создания зависимости логики от Room
 RoomFile будет представлять таблицу RoomFile*/

@Entity
data class RoomFile(
    @PrimaryKey(autoGenerate = true)
    var idFile: Long = 0L,
    var delay: Int = 6
)