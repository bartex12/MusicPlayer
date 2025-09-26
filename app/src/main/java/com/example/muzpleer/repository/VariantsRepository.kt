package com.example.muzpleer.repository

import com.example.muzpleer.room.dao.PlaylistDao
import com.example.muzpleer.room.dao.SongDao
import com.example.muzpleer.room.dao.VariantsDao

class VariantsRepository(private val variantsDao: VariantsDao,
                         private val songDao: SongDao) {

}