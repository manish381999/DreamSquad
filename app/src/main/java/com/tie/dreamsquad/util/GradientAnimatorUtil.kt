package com.tie.dreamsquad.utils

import android.animation.ArgbEvaluator
import android.animation.ValueAnimator
import android.content.Context
import android.graphics.drawable.GradientDrawable
import android.view.View
import androidx.core.content.ContextCompat
import com.tie.dreamsquad.R

object GradientAnimatorUtil {

    // Function to start gradient animation
    fun startGradientAnimation(context: Context, targetView: View) {
        // Retrieve and initialize the gradient drawable
        val gradientDrawable = ContextCompat.getDrawable(context, R.drawable.gradient_background) as? GradientDrawable
        targetView.background = gradientDrawable

        // Define colors for the gradient animation
        val colorFrom = ContextCompat.getColor(context, R.color.gradient_start)
        val colorTo = ContextCompat.getColor(context, R.color.gradient_end)
        val colorAlt = ContextCompat.getColor(context, R.color.gradient_alt)

        // Set up ValueAnimator to animate background colors
        val colorAnimator = ValueAnimator.ofObject(ArgbEvaluator(), colorFrom, colorTo, colorAlt)
        colorAnimator.duration = 10000L  // Duration in milliseconds
        colorAnimator.repeatCount = ValueAnimator.INFINITE  // Repeat indefinitely
        colorAnimator.repeatMode = ValueAnimator.REVERSE  // Reverse back and forth

        // Update the gradient drawable colors during animation
        colorAnimator.addUpdateListener { animator ->
            val animatedColor = animator.animatedValue as Int
            gradientDrawable?.setColors(intArrayOf(animatedColor, colorTo))
            targetView.background = gradientDrawable
        }

        // Start the animation
        colorAnimator.start()
    }
}
