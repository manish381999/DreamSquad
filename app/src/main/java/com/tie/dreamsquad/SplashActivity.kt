package com.tie.dreamsquad

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.net.ConnectivityManager
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.animation.AnimationUtils
import android.widget.Button
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
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

        // Check permissions for notifications and storage
        checkAndRequestPermissions()
    }

    private fun checkAndRequestPermissions() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ (API 33) requires separate permission for notifications
            if (!hasNotificationPermission()) {
                requestNotificationPermission()
            }
        }
        if (!hasStoragePermissions()) {
            requestStoragePermissions()
        } else if (isNetworkAvailable()) {
            // Delay transition to LoginActivity if permissions and network are available
            navigateToLogin()
        } else {
            showNoInternetDialog()
        }
    }

    private fun hasNotificationPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
        } else {
            true // No need to check for Android < 13.
        }
    }

    private fun requestNotificationPermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (!isGranted) {
            // Handle notification permission denied, if needed
        }
    }

    private fun hasStoragePermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED &&
                ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestStoragePermissions() {
        ActivityCompat.requestPermissions(
            this,
            arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE),
            STORAGE_PERMISSION_REQUEST_CODE
        )
    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        if (requestCode == STORAGE_PERMISSION_REQUEST_CODE) {
            if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                // Permissions granted, proceed to check network
                if (isNetworkAvailable()) {
                    navigateToLogin()
                } else {
                    showNoInternetDialog()
                }
            } else {
                // Permissions denied, inform the user
                showPermissionDeniedDialog()
            }
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
            if (isNetworkAvailable()) {
                navigateToLogin()
            } else {
                showNoInternetDialog()
            }
        }
    }

    private fun showPermissionDeniedDialog() {
        AlertDialog.Builder(this)
            .setTitle("Permissions Required")
            .setMessage("Storage permissions are required to continue.")
            .setPositiveButton("OK") { _, _ ->
                requestStoragePermissions()
            }
            .setCancelable(false)
            .show()
    }

    private fun navigateToLogin() {
        Handler(Looper.getMainLooper()).postDelayed({
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
        }, 3000) // 3 seconds delay
    }

    companion object {
        private const val STORAGE_PERMISSION_REQUEST_CODE = 1001
    }
}
