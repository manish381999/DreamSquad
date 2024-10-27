package com.tie.dreamsquad

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tie.dreamsquad.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {

    // Declare the ViewBinding object
    private lateinit var binding: ActivityMainBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Initialize View Binding
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Ensure `gradient_background` is a valid drawable reference
        val gradientDrawable = ContextCompat.getDrawable(this, R.drawable.gradient_background) as? GradientDrawable
        if (gradientDrawable != null) {
            binding.mainLayout.background = gradientDrawable
        } else {
            // Log or handle the case where the drawable could not be loaded
        }


        // Define colors for the gradient animation
        val colorFrom = ContextCompat.getColor(this, R.color.gradient_start)
        val colorTo = ContextCompat.getColor(this, R.color.gradient_end)
        val colorAlt = ContextCompat.getColor(this, R.color.gradient_alt)

        // Set up ValueAnimator to animate background colors
        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo, colorAlt)
        colorAnimator.duration = 4000L  // Duration in milliseconds
        colorAnimator.repeatCount = ValueAnimator.INFINITE  // Repeat indefinitely
        colorAnimator.repeatMode = ValueAnimator.REVERSE  // Reverse back and forth

        // Update the gradient drawable colors during animation
        colorAnimator.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            gradientDrawable?.setColors(intArrayOf(animatedColor, colorTo))  // Apply animated colors
            binding.mainLayout.background = gradientDrawable
        }

        // Start the animation
        colorAnimator.start()
    }
}
