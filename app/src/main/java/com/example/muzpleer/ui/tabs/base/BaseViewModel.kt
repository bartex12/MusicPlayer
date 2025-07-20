package com.example.muzpleer.ui.tabs.base

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.muzpleer.model.MusicTrack
import kotlinx.coroutines.*

open class BaseViewModel(
    private val storage:BaseStorage
): ViewModel() {
    private val data:MutableLiveData<List<MusicTrack>> = MutableLiveData<List<MusicTrack>>() //список файлов

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

    fun getMyTracks() : LiveData<List<MusicTrack>> {
        loadData()
        return data
    }

    private fun loadData() {
        //останавливаем запущенные корутины, так как они уже не нужны при запросе новых данных
        cancelJob()
        // Запускаем корутину для асинхронного доступа к серверу с помощью  launch
        // не кладем данные, если они не изменились, чтобы не было дублирования -
        // так как вызов  метода getRascladki из 3 мест
        viewModelCoroutineScope.launch {
            val dataNew = storage.getMyTracksList()
            if (dataNew!=data.value){
                data.postValue(dataNew)
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
}