package com.example.muzpleer

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import androidx.preference.PreferenceManager
import com.example.muzpleer.databinding.ActivitySplashBinding

class SplashActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val  isShowScreen = PreferenceManager.getDefaultSharedPreferences(this)
            .getBoolean("cbScreen", true)
        if(isShowScreen){
            binding = ActivitySplashBinding.inflate(layoutInflater)
            setContentView(binding.root)

            Handler(Looper.getMainLooper()).postDelayed({
                binding.imageViewSplash.animate()
                    .scaleY(2f)
                    .scaleX(2f)
                    .setInterpolator(LinearInterpolator()).setDuration(2000)
                    .setListener(object : AnimatorListenerAdapter(){
                        override fun onAnimationEnd(animation: Animator) {
                            super.onAnimationEnd(animation)
                            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                            finish()
                        }
                    })
            }, 300)
        }else{
            startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            finish()
        }
    }
}