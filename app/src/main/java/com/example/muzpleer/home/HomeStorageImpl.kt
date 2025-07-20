package com.example.muzpleer.home

import android.content.Context
import androidx.core.content.ContextCompat
import com.example.muzpleer.R
import com.example.muzpleer.model.DataHome
import java.util.*

class HomeStorageImpl(private val app:Context ): HomeStorage{

    companion object{const val TAG = "33333"}

    override fun getListMain(): ArrayList<DataHome> {
        val stringListMain = app.resources.getStringArray(R.array.MenuMain)
        val stringListSubMain = app.resources.getStringArray(R.array.MenuSubMain)

        val list :ArrayList<DataHome> = arrayListOf()
        val localTracks = ContextCompat.getDrawable(app, R.drawable.placeholder2)
        val myTracks = ContextCompat.getDrawable(app, R.drawable.rose1)
        val settings = ContextCompat.getDrawable(app, R.drawable.settings1)
        localTracks?.let {
            list.add( DataHome(picture = it, head= stringListMain[0], subHead = stringListSubMain[0]))
        }
        myTracks?.let {
            list.add(
                DataHome(picture = it, head= stringListMain[1], subHead = stringListSubMain[1]))
        }

        settings?.let{
            list.add(
                DataHome(picture = it, head= stringListMain[2], subHead = stringListSubMain[2]))
        }
        return list
    }
}
//val kingTracks = ContextCompat.getDrawable(app, R.drawable.king)
//        kingTracks?.let {
//            list.add(
//                DataHome(picture = it, head= stringListMain[2], subHead = stringListSubMain[2]))
//        }