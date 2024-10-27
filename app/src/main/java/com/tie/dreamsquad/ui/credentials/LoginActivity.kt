package com.tie.dreamsquad.ui.credentials

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.Editable
import android.text.SpannableString
import android.text.Spanned
import android.text.TextWatcher
import android.text.style.ForegroundColorSpan
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.tie.dreamsquad.R
import com.tie.dreamsquad.api.ApiClient
import com.tie.dreamsquad.databinding.ActivityLoginBinding
import com.tie.dreamsquad.ui.credentials.model.ImageData
import com.tie.dreamsquad.ui.credentials.model.ImageResponse
import com.tie.dreamsquad.ui.credentials.model.OtpResponse
import com.tie.dreamsquad.util.SliderAdapter
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding
    private lateinit var sliderAdapter: SliderAdapter
    private var imageDataList: MutableList<ImageData> = mutableListOf()
    private val autoScrollHandler = Handler(Looper.getMainLooper())

    private val autoScrollRunnable = object : Runnable {
        override fun run() {
            val currentItem = binding.vpImageSlider.currentItem
            val nextItem = if (currentItem + 1 < sliderAdapter.itemCount) currentItem + 1 else 0
            binding.vpImageSlider.currentItem = nextItem
            autoScrollHandler.postDelayed(this, 3000) // Auto-scroll every 3 seconds
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sliderAdapter = SliderAdapter(this, imageDataList)
        binding.vpImageSlider.adapter = sliderAdapter

        fetchImageUrls()
        setTermsAndConditionsText() // Set colored T&C text
        // Create Notification Channel
        createNotificationChannel()

        binding.btnContinue.setOnClickListener {
            if (checkValidation()) {
                // Proceed with the action, like sending the OTP
                loginWithOtp()
            }
        }

        // TextInputEditText and CheckBox listener to enable button based on conditions
        binding.tilMobileNo.editText?.addTextChangedListener(textWatcher)
        binding.CheckBox.setOnCheckedChangeListener { _, isChecked ->
            updateContinueButtonState()
            // Apply color change for checkbox when checked/unchecked
            val color = if (isChecked) R.color.checked_color else R.color.unchecked_color
            binding.CheckBox.buttonTintList = ContextCompat.getColorStateList(this, color)
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




    private fun checkValidation(): Boolean {
        // Retrieve the input from the TextInputEditText inside the TextInputLayout
        val mobileNumber = binding.tilMobileNo.editText?.text.toString().trim()

        return when {
            mobileNumber.isEmpty() -> {
                // Show an error message if the input is empty
                binding.tilMobileNo.error = "Mobile number cannot be empty"
                false
            }
            mobileNumber.length < 10 -> {
                // Show an error message if the input is less than 10 digits
                binding.tilMobileNo.error = "Mobile number must be 10 digits"
                false
            }
            else -> {
                // Clear any previous error if the input is valid
                binding.tilMobileNo.error = null
                true
            }
        }
    }


    private val textWatcher = object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            updateContinueButtonState()
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    }

    // Update the Continue button based on input field and checkbox state
    private fun updateContinueButtonState() {
        val isCheckboxChecked = binding.CheckBox.isChecked
        val isMobileNumberEntered = !binding.tilMobileNo.editText?.text.isNullOrBlank()

        if (isCheckboxChecked && isMobileNumberEntered) {
            binding.btnContinue.apply {
                isEnabled = true
                setBackgroundResource(R.drawable.bg_enable_continue) // Enabled background
                setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.white))
            }
        } else {
            binding.btnContinue.apply {
                isEnabled = false
                setBackgroundResource(R.drawable.bg_disable_continue) // Disabled background
                setTextColor(ContextCompat.getColor(this@LoginActivity, R.color.gray2))
            }
        }
    }

    private fun setTermsAndConditionsText() {
        val fullText = "By continuing, I agree to DreamSquad's T&C."
        val spannable = SpannableString(fullText)

        // Find the index of "T&C" to color it
        val start = fullText.indexOf("T&C")
        val end = start + "T&C".length

        // Set color for "T&C"
        spannable.setSpan(ForegroundColorSpan(Color.parseColor("#FF0000")), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE) // Adjust color as needed

        // Set the spannable text to the TextView
        binding.tvAgree.text = spannable
    }

    private fun fetchImageUrls() {
        ApiClient.api.getImageUrls().enqueue(object : Callback<ImageResponse> {
            override fun onResponse(call: Call<ImageResponse>, response: Response<ImageResponse>) {
                if (response.isSuccessful && response.body()?.status == "success") {
                    val images = response.body()?.images ?: emptyList()
                    sliderAdapter.updateImages(images)
                    if (images.isNotEmpty()) {
                        autoScrollHandler.postDelayed(autoScrollRunnable, 3000)
                    }
                } else {
                    // Handle the unsuccessful response case
                }
            }

            override fun onFailure(call: Call<ImageResponse>, t: Throwable) {
                // Handle the error case
            }
        })
    }

    private fun loginWithOtp() {
        val mobileNumber = binding.tilMobileNo.editText?.text.toString().trim()

        ApiClient.api.loginWithOtp(mobileNumber)
            .enqueue(object : retrofit2.Callback<OtpResponse> {
                override fun onResponse(call: Call<OtpResponse>, response: retrofit2.Response<OtpResponse>) {
                    if (response.isSuccessful) {
                        val otpResponse = response.body()
                        if (otpResponse != null && otpResponse.status == "success") {
                            // Print OTP to Logcat
                            Log.d("LoginWithOtp", "OTP: ${otpResponse.otp}")

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

    private fun showOtpNotification(otp: Int) {
        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        // Build the notification
        val notification = NotificationCompat.Builder(this, "OTPChannel")
            .setSmallIcon(R.drawable.app_logo)  // Replace with your app's icon
            .setContentTitle("Your OTP Code")
            .setContentText("Your OTP is $otp")
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        // Show the notification
        notificationManager.notify(1, notification)
    }


    override fun onDestroy() {
        super.onDestroy()
        autoScrollHandler.removeCallbacks(autoScrollRunnable)
    }
}
