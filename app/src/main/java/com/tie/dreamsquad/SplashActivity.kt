package com.tie.dreamsquad

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import androidx.appcompat.app.AppCompatActivity
import com.tie.dreamsquad.databinding.ActivitySplashBinding
import com.tie.dreamsquad.ui.credentials.LoginActivity

class SplashActivity : AppCompatActivity() {
    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Load the scale and fade animation
        val scaleFadeAnim = AnimationUtils.loadAnimation(this, R.anim.scale_fade)
        binding.appLogo.startAnimation(scaleFadeAnim)

        // Delay transition to MainActivity
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
//            finish()
        }, 3000) // 3 seconds delay
    }
}
