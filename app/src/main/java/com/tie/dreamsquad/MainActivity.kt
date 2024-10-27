package com.tie.dreamsquad

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.tie.dreamsquad.databinding.ActivityMainBinding
import com.tie.dreamsquad.utils.GradientAnimatorUtil  // Import the utility

class MainActivity : AppCompatActivity() {

    // Declare the ViewBinding object
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Start the gradient animation
        GradientAnimatorUtil.startGradientAnimation(this, binding.mainLayout)
    }
}
