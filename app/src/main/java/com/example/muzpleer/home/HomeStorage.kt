package com.example.muzpleer.home

import com.example.muzpleer.model.DataHome
import java.util.ArrayList

interface HomeStorage {
    fun getListMain(): ArrayList<DataHome>
//    suspend fun createDefaultFile(fileName:String)
//    suspend fun getAllFiles():List<RoomFile>
}