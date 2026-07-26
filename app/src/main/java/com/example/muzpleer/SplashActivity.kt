package com.example.muzpleer

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import com.example.muzpleer.ui.local.viewmodel.SharedViewModel
import org.koin.android.ext.android.inject

class SplashActivity: AppCompatActivity() {

    val viewModel: SharedViewModel by inject()   // если используете Koin в Activity

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)
        // Запускаем загрузку данных
        loadData()
    }

    private fun loadData() {
       viewModel.startSplash{
           // После загрузки переходим в MainActivity
           startActivity(Intent(this, MainActivity::class.java))
           finish()
       }

    }
}