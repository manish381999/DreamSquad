package com.tie.dreamsquad

import android.content.Intent
import android.net.ConnectivityManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
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

        // Check internet connection
        if (!isNetworkAvailable()) {
            showNoInternetDialog()
        } else {
            // Delay transition to LoginActivity
            Handler(Looper.getMainLooper()).postDelayed({
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Finish SplashActivity to remove it from the back stack
            }, 3000) // 3 seconds delay
        }
    }

    private fun isNetworkAvailable(): Boolean {
        val connectivityManager = getSystemService(CONNECTIVITY_SERVICE) as ConnectivityManager
        val networkInfo = connectivityManager.activeNetworkInfo
        return networkInfo != null && networkInfo.isConnected
    }

    private fun showNoInternetDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_no_internet, null)
        val alertDialogBuilder = AlertDialog.Builder(this)
            .setView(dialogView)
            .setCancelable(false)

        val dialog = alertDialogBuilder.create()
        dialog.show()

        // Retry button listener
        val retryButton: Button = dialogView.findViewById(R.id.button_retry)
        retryButton.setOnClickListener {
            dialog.dismiss()
            // Check internet connection again
            if (isNetworkAvailable()) {
                startActivity(Intent(this, LoginActivity::class.java))
                finish() // Finish SplashActivity
            } else {
                // Show dialog again if still no connection
                showNoInternetDialog()
            }
        }
    }
}
