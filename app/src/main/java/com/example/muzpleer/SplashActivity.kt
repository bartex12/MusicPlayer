package com.example.muzpleer

import android.animation.Animator
import android.content.Intent
import android.os.Bundle
import android.view.animation.LinearInterpolator
import androidx.appcompat.app.AppCompatActivity
import com.example.muzpleer.databinding.ActivitySplashBinding

class SplashActivity: AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.imageViewSplash.animate()
            .scaleY(2.5f)
            .scaleX(2.5f)
            .setInterpolator(LinearInterpolator()).setDuration(1500)
            .setListener(object : Animator.AnimatorListener {

                override fun onAnimationStart(p0: Animator) {}
                override fun onAnimationEnd(p0: Animator) {
                    startActivity(Intent(this@SplashActivity, MainActivity::class.java))
                    finish()
                }
                override fun onAnimationCancel(p0: Animator) {}
                override fun onAnimationRepeat(p0: Animator) {}
            })

//        window.apply {
//            clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS)
//            addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS)
//            statusBarColor = android.graphics.Color.TRANSPARENT
//            decorView.systemUiVisibility = View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN or
//                    View.SYSTEM_UI_FLAG_LAYOUT_STABLE
//        }
//
//        // Просто ждём 2 секунды и переходим, а сканирование запустится в MainActivity
//        Handler(Looper.getMainLooper()).postDelayed({
//            startActivity(Intent(this, MainActivity::class.java))
//            finish()
//        }, 1000)
    }
}