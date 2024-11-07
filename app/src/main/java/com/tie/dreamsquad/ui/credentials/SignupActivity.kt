package com.tie.dreamsquad.ui.credentials

import android.app.Activity
import android.content.ContentResolver
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.util.Log
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.addTextChangedListener
import com.tie.dreamsquad.R
import com.tie.dreamsquad.databinding.ActivitySignupBinding
import com.tie.dreamsquad.ui.credentials.model.UserDetails
import com.tie.dreamsquad.api.ApiClient
import com.tie.dreamsquad.utils.SP
import okhttp3.MediaType
import okhttp3.MultipartBody
import okhttp3.RequestBody
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import java.io.File

class SignupActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding
    private var imageUri: Uri? = null // To store the image URI
    private var imagePath: String? = null // To store the image path

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        initComponents()
        clickListeners()
    }

    private fun initComponents() {
        // Set initial state of the button
        binding.btnSignupContinue.isEnabled = false
        binding.btnSignupContinue.background = resources.getDrawable(R.drawable.bg_disable_continue, theme) // Initially set disabled background
    }

    private fun clickListeners() {
        // Click listener for the profile picture add button
        binding.ivAddProfilePic.setOnClickListener {
            openGallery()
        }

        // Add text change listeners to validate fields
        binding.tilName.editText?.addTextChangedListener {
            validateInputs()
        }

        binding.tilEmail.editText?.addTextChangedListener {
            validateInputs()
        }

        // Click listener for the sign-up button
        binding.btnSignupContinue.setOnClickListener {
            // Proceed with sign-up logic here
            val name = binding.tilName.editText?.text.toString()
            val email = binding.tilEmail.editText?.text.toString()

            if (imagePath != null && name.isNotEmpty() && email.isNotEmpty()) {
                // Call the API to update user details
                updateUserDetails(name, email)
            } else {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun updateUserDetails(name: String, email: String) {
        val userId = SP.getPreferences(this, SP.USER_ID)

        val profilePicPart: MultipartBody.Part? = if (imagePath != null) {
            val file = File(imagePath!!)
            val requestBody = RequestBody.create(MediaType.parse("image/*"), file)
            MultipartBody.Part.createFormData("profile_pic", file.name, requestBody)
        } else {
            null
        }

        val idRequestBody = RequestBody.create(MultipartBody.FORM, userId)
        val nameRequestBody = RequestBody.create(MultipartBody.FORM, name)
        val emailRequestBody = RequestBody.create(MultipartBody.FORM, email)


        ApiClient.api.updateUserDetails(
            idRequestBody,
            nameRequestBody,
            emailRequestBody,
            profilePicPart
        )
            .enqueue(object : Callback<UserDetails> {
                override fun onResponse(call: Call<UserDetails>, response: Response<UserDetails>) {
                    if (response.isSuccessful && response.body() != null) {
                        val userDetails = response.body()!!

                        // Log the response status and message
                        Log.d("API Response", "Status: ${userDetails.status}")
                        Log.d("API Response", "Message: ${userDetails.message}")

                        if (userDetails.status == "success") {
                            // Handle successful response
                            Toast.makeText(
                                this@SignupActivity,
                                userDetails.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            val updatedUser = userDetails.user

                            // Log updated user details
                            updatedUser?.let {
                                Log.d(
                                    "API Response",
                                    "Updated User: ${it.name}, ${it.email_id}, ${it.profile_pic}"
                                )

                                // Example: update UI or navigate to another activity
                                Toast.makeText(
                                    this@SignupActivity,
                                    "Welcome, ${it.name}",
                                    Toast.LENGTH_SHORT
                                ).show()
                            }
                        } else {
                            Toast.makeText(
                                this@SignupActivity,
                                userDetails.message,
                                Toast.LENGTH_SHORT
                            ).show()
                            Log.d("API Response", "Error: ${userDetails.message}")
                        }
                    } else {
                        Toast.makeText(this@SignupActivity, "API call failed", Toast.LENGTH_SHORT)
                            .show()
                        // Log the error response
                        Log.d("API Response", "Response failed: ${response.errorBody()?.string()}")
                    }
                }

                override fun onFailure(call: Call<UserDetails>, t: Throwable) {
                    // Handle failure response
                    Toast.makeText(this@SignupActivity, "Error: ${t.message}", Toast.LENGTH_SHORT)
                        .show()
                    // Log the error message
                    Log.e("API Error", "API call failed: ${t.message}", t)
                }
            })
    }

    private fun openGallery() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        galleryResultLauncher.launch(intent)
    }

    private val galleryResultLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            imageUri = result.data?.data
            // Display the image on the profile picture view
            binding.ivProfilePic.setImageURI(imageUri)
            // Extract and store the image path
            imagePath = getImagePathFromUri(imageUri)
            validateInputs()  // Revalidate inputs after image selection
        }
    }

    private fun getImagePathFromUri(uri: Uri?): String? {
        if (uri == null) return null

        // Handle different Uri schemes (File, Content)
        val scheme = uri.scheme
        if (scheme == "content") {
            val cursor: Cursor? = contentResolver.query(uri, null, null, null, null)
            cursor?.let {
                val columnIndex = it.getColumnIndex(MediaStore.Images.Media.DATA)
                it.moveToFirst()
                val path = it.getString(columnIndex)
                it.close()
                return path
            }
        } else if (scheme == "file") {
            return uri.path
        }
        return null
    }

    private fun validateInputs() {
        val name = binding.tilName.editText?.text.toString()
        val email = binding.tilEmail.editText?.text.toString()

        // Check if both name and email are valid
        val isNameValid = name.isNotEmpty()
        val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()
        val isImageSelected = imageUri != null

        // Enable the button and change the background if all conditions are met
        if (isNameValid && isEmailValid && isImageSelected) {
            binding.btnSignupContinue.isEnabled = true
            binding.btnSignupContinue.background = resources.getDrawable(R.drawable.bg_enable_continue, theme) // Set the enabled background
        } else {
            binding.btnSignupContinue.isEnabled = false
            binding.btnSignupContinue.background = resources.getDrawable(R.drawable.bg_disable_continue, theme) // Set the disabled background
        }
    }
}
