package com.tie.dreamsquad.ui.credentials.model

import com.google.gson.annotations.SerializedName

data class OtpResponse(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("otp") val otp: Int
)
