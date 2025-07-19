package com.example.muzpleer.home

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.muzpleer.model.DataHome

class HomeViewModel( private val storage:HomeStorage): ViewModel() {

    companion object{const val TAG = "33333"    }

    private val data: MutableLiveData<ArrayList<DataHome>> = MutableLiveData()

    fun getListMain(): LiveData<ArrayList<DataHome>> {
        loadData()
        return data
    }

    private fun loadData() {
        data.value = storage.getListMain()
    }
}