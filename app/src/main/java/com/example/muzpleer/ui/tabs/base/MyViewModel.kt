package com.example.muzpleer.ui.tabs.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.muzpleer.model.Song
import com.example.muzpleer.util.Constants
import kotlinx.coroutines.*

open class MyViewModel(
    private val storage:MyStorage
): ViewModel() {
    private val _data:MutableLiveData<List<Song>> = MutableLiveData<List<Song>>() //список файлов
    val data:LiveData<List<Song>> = _data

    private val _dataMy:MutableLiveData<List<Song>> = MutableLiveData<List<Song>>() //список файлов
    val dataMy:LiveData<List<Song>> = _dataMy

    private val _dataKing:MutableLiveData<List<Song>> = MutableLiveData<List<Song>>() //список файлов
    val dataKing:LiveData<List<Song>> = _dataKing

//     Объявляем свой собственный скоуп
//     В качестве аргумента передается CoroutineContext - через "+" из трех частей:
//     - Dispatchers.Main говорит, что результат работы предназначен для основного потока;
//     - SupervisorJob() позволяет всем дочерним корутинам выполняться
//     независимо  - если какая-то корутина упадёт с ошибкой, остальные будут выполнены нормально;
//     - CoroutineExceptionHandler позволяет перехватывать и отрабатывать ошибки и краши
    private val viewModelCoroutineScope = CoroutineScope(
        Dispatchers.Main
                + SupervisorJob()
                + CoroutineExceptionHandler { _, throwable ->
            handleError(throwable)
        })

companion object {
   const val TAG ="33333"
}

   init { loadData() }

    private fun loadData() {
        //останавливаем запущенные корутины, так как они уже не нужны при запросе новых данных
        cancelJob()
        // Запускаем корутину для асинхронного доступа к серверу с помощью  launch
        // не кладем данные, если они не изменились, чтобы не было дублирования -
        // так как вызов  метода getRascladki из 3 мест
        viewModelCoroutineScope.launch {
            val dataNew = storage.getMyTracksList()
            val dataNewMy =dataNew.filter { it.typeFromIfMy == Constants.MY_TRACK }
            val dataNewKing  =dataNew.filter { it.typeFromIfMy == Constants.LITTLE_KING }
            if (dataNew!=_data.value){
                _data.postValue(dataNew)
                _dataMy.postValue(dataNewMy)
                _dataKing.postValue(dataNewKing)
            }
        }
    }

    // Завершаем все незавершённые корутины, потому что пользователь закрыл экран
    //отдельный метод - так как используется в нескольких местах
    private fun cancelJob() {
        viewModelCoroutineScope.coroutineContext.cancelChildren()
    }

    //обрабатываем ошибки в конкретной имплементации базовой ВьюМодели
    private fun handleError(error: Throwable){
        Log.d(TAG, "BaseViewModel HandleError: error = ${error.message} ")
    }

    override fun onCleared() {
        super.onCleared()
        cancelJob()
    }

    fun getDataKing():List<Song>{
        return dataKing.value
    }

    fun getDataMy():List<Song>{
        return dataMy.value
    }
}