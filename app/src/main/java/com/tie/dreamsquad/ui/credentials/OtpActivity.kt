package com.tie.dreamsquad.ui.credentials

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.Toast
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.text.Spanned
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tie.dreamsquad.MainActivity
import com.tie.dreamsquad.R
import com.tie.dreamsquad.api.ApiClient
import com.tie.dreamsquad.ui.credentials.model.UserDetails
import com.tie.dreamsquad.databinding.ActivityOtpBinding
import com.tie.dreamsquad.ui.credentials.model.OtpResponse
import com.tie.dreamsquad.utils.SP
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

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

        createNotificationChannel()

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


    private fun createNotificationChannel() {
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "OTPChannel",
                "OTP Notifications",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Channel for OTP notifications"
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
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
                // Call API to verify OTP
                verifyOtpApi(mobileNumber, receivedOtp)
            }
        }
    }

    private fun verifyOtpApi(mobileNumber: String?, otp: String?) {
        // Check if mobileNumber and OTP are not null
        if (mobileNumber != null && otp != null) {
            ApiClient.api.verifyOtp(mobileNumber, otp).enqueue(object : Callback<UserDetails> {
                override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                    if (response.isSuccessful) {
                        val userDetails = response.body()

                        if (userDetails != null && userDetails.status == "success") {
                            // OTP verified successfully
                            Toast.makeText(this@OtpActivity, "OTP Verified Successfully", Toast.LENGTH_SHORT).show()

                            // Save user data in SharedPreferences
                            SP.savePreferences(this@OtpActivity, SP.USER_ID, userDetails.user?.id.toString())
                            SP.savePreferences(this@OtpActivity, SP.USER_MOBILE, userDetails.user?.mobile_number)
                            SP.savePreferences(this@OtpActivity, SP.USER_NAME, userDetails.user?.name)
                            SP.savePreferences(this@OtpActivity, SP.USER_EMAIL, userDetails.user?.email_id)
                            SP.savePreferences(this@OtpActivity, SP.USER_PROFILE_PIC, userDetails.user?.profile_pic)
                            SP.savePreferences(this@OtpActivity, SP.USER_IS_VERIFIED, userDetails.user?.is_verified.toString())
                            SP.savePreferences(this@OtpActivity, SP.USER_STATUS, userDetails.user?.status.toString())
                            SP.savePreferences(this@OtpActivity, SP.USER_CREATED_AT, userDetails.user?.created_at)

                            // Save LOGIN_STATUS as true (user is logged in)
                            SP.savePreferences(this@OtpActivity, SP.LOGIN_STATUS, "true")

                            // Check if name and email are null
                            if (userDetails.user?.name.isNullOrEmpty() || userDetails.user?.email_id.isNullOrEmpty()) {
                                // If name or email is null, navigate to SignupActivity
                                val intent = Intent(this@OtpActivity, SignupActivity::class.java)
                                startActivity(intent)
                                finish()
                            } else {
                                // If name and email are not null, navigate to MainActivity
                                val intent = Intent(this@OtpActivity, MainActivity::class.java)
                                startActivity(intent)
                                finish()
                            }
                        } else {
                            // OTP verification failed
                            Toast.makeText(this@OtpActivity, "OTP Verification Failed: ${userDetails?.message}", Toast.LENGTH_SHORT).show()
                        }
                    } else {
                        Toast.makeText(this@OtpActivity, "API Error: ${response.code()}", Toast.LENGTH_SHORT).show()
                    }
                }

                override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                    // Handle network failure or other issues
                    Toast.makeText(this@OtpActivity, "Failed to verify OTP: ${t.message}", Toast.LENGTH_SHORT).show()
                }
            })
        } else {
            // Handle the case where either mobileNumber or otp is null
            Toast.makeText(this@OtpActivity, "Mobile number or OTP is missing.", Toast.LENGTH_SHORT).show()
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
                val resendText = "Didn't receive OTP? Resend OTP"
                val spannableString = SpannableString(resendText)

                // Get the starting index of "Resend OTP"
                val startIndex = resendText.indexOf("Resend OTP")

                // Apply a red color to "Resend OTP" part
                spannableString.setSpan(
                    ForegroundColorSpan(ContextCompat.getColor(this@OtpActivity, R.color.black)),
                    startIndex,
                    resendText.length,
                    Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                )

                // Set the styled text to the TextView
                binding.tvResendOtp.text = spannableString

                // Set the onClickListener for the "Resend OTP" part
                binding.tvResendOtp.setOnClickListener {
                    resendOtp()
                }
            }
        }.start()
        timerRunning = true
    }

    private fun resendOtp() {
        // Implement the logic to resend the OTP here
        loginWithOtp()
    }

    private fun loginWithOtp() {
        mobileNumber?.let {
            ApiClient.api.loginWithOtp(it)
                .enqueue(object : retrofit2.Callback<OtpResponse> {
                    override fun onResponse(
                        call: Call<OtpResponse>,
                        response: retrofit2.Response<OtpResponse>
                    ) {
                        if (response.isSuccessful) {
                            val otpResponse = response.body()
                            if (otpResponse != null && otpResponse.status == "success") {
                                // Print OTP to Logcat
                                Log.d("LoginWithOtp", "OTP: ${otpResponse.otp}")
                                startTimer() // Restart the timer if OTP is successfully resent

                                // Show OTP in a Notification
                                showOtpNotification(otpResponse.otp)
                            } else {
                                Log.d("LoginWithOtp", "Failed to send OTP: ${otpResponse?.message}")
                            }
                        } else {
                            Log.e("LoginWithOtp", "Response not successful: ${response.code()}")
                        }
                    }

                    override fun onFailure(call: Call<OtpResponse>, t: Throwable) {
                        Log.e("LoginWithOtp", "API call failed: ${t.message}")
                    }
                })
        }
    }

    private fun showOtpNotification(otp: String) {
        val notificationManager =
            getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val notification = NotificationCompat.Builder(this, "OTPChannel")
            .setSmallIcon(R.drawable.app_logo)  // Replace with your app's icon
            .setContentTitle("DreamSquad")
            .setContentText("$otp is your OTP for DreamSquad")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }

    override fun onDestroy() {
        super.onDestroy()
        if (timerRunning) {
            countDownTimer.cancel() // Cancel the timer if activity is destroyed
        }
    }
}
