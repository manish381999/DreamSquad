package com.tie.dreamsquad.ui.credentials

import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.tie.dreamsquad.R
import com.tie.dreamsquad.databinding.ActivityOtpBinding

class OtpActivity : AppCompatActivity() {
    private lateinit var binding: ActivityOtpBinding
    private var receivedOtp: String? = null
    private var mobileNumber: String? = null
    private lateinit var countDownTimer: CountDownTimer
    private var timerRunning: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOtpBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Retrieve the mobile number and OTP from the intent
        mobileNumber = intent.getStringExtra("mobile_number")
        receivedOtp = intent.getStringExtra("otp")

        Log.d("OtpActivity", "Received OTP: $receivedOtp for mobile: $mobileNumber")

        initComponents()
        clickListeners()
        startTimer()
    }

    private fun initComponents() {
        val otpFields = listOf(
            binding.etOtp1, binding.etOtp2, binding.etOtp3,
            binding.etOtp4, binding.etOtp5, binding.etOtp6
        )

        otpFields.forEachIndexed { index, editText ->
            editText.addTextChangedListener(object : TextWatcher {
                override fun afterTextChanged(s: Editable?) {
                    if (s?.length == 1 && index < otpFields.size - 1) {
                        otpFields[index + 1].requestFocus() // Move to next field if a digit is entered
                    } else if (s?.isEmpty() == true && index > 0) {
                        otpFields[index - 1].requestFocus() // Move to previous field if this is cleared
                    }
                    validateOtp() // Validate OTP after each input
                }

                override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
                override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
            })
        }
    }

    private fun validateOtp() {
        // Get the entered OTP from all fields
        val enteredOtp = binding.etOtp1.text.toString() +
                binding.etOtp2.text.toString() +
                binding.etOtp3.text.toString() +
                binding.etOtp4.text.toString() +
                binding.etOtp5.text.toString() +
                binding.etOtp6.text.toString()

        Log.d("OtpActivity", "Entered OTP: $enteredOtp")
        Log.d("OtpActivity", "Received OTP: $receivedOtp")

        // Check if the entered OTP matches the received OTP
        val isOtpCorrect = enteredOtp == receivedOtp

        binding.btnSubmit.apply {
            isEnabled = isOtpCorrect
            if (isOtpCorrect) {
                setBackgroundResource(R.drawable.bg_enable_continue)
                setTextColor(ContextCompat.getColor(this@OtpActivity, R.color.white))
            } else {
                setBackgroundResource(R.drawable.bg_disable_continue)
                setTextColor(ContextCompat.getColor(this@OtpActivity, R.color.gray2))
            }
            invalidate() // Ensure UI updates
        }

        // Show message if OTP does not match and all fields are filled
        if (enteredOtp.length == 6 && !isOtpCorrect) {
            Toast.makeText(this, "Incorrect OTP. Please try again.", Toast.LENGTH_SHORT).show()
        }
    }

    private fun clickListeners() {
        // Handle OTP submission
        binding.btnSubmit.setOnClickListener {
            if (binding.btnSubmit.isEnabled) {
                Toast.makeText(this, "OTP Verified Successfully", Toast.LENGTH_SHORT).show()
                // Proceed to the next screen or action
            }
        }
    }

    private fun startTimer() {
        binding.tvResendOtp.text = "Didn't receive OTP? 30s" // Initial message with timer
        countDownTimer = object : CountDownTimer(30000, 1000) {
            override fun onTick(millisUntilFinished: Long) {
                val secondsRemaining = millisUntilFinished / 1000
                binding.tvResendOtp.text = "Didn't receive OTP? ${secondsRemaining}s"
            }

            override fun onFinish() {
                binding.tvResendOtp.text = "Didn't receive OTP? Resend OTP"
                binding.tvResendOtp.setTextColor(ContextCompat.getColor(this@OtpActivity, R.color.black))
                binding.tvResendOtp.setOnClickListener {
                    resendOtp()
                }
            }
        }.start()
        timerRunning = true
    }

    private fun resendOtp() {
        // Implement the logic to resend the OTP here
        Toast.makeText(this, "Resending OTP...", Toast.LENGTH_SHORT).show()
        // Here you would call your API to resend the OTP and restart the timer if needed
        startTimer() // Restart the timer if OTP is successfully resent
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timerRunning) {
            countDownTimer.cancel() // Cancel the timer if activity is destroyed
        }
    }
}
