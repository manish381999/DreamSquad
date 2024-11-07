package com.tie.dreamsquad.api

import com.tie.dreamsquad.ui.credentials.model.ImageResponse
import com.tie.dreamsquad.ui.credentials.model.OtpResponse
import com.tie.dreamsquad.ui.credentials.model.UserDetails
import retrofit2.Call
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiEndPointInterface {

    @GET("get_login_screen_image.php")
    fun getImageUrls(): Call<ImageResponse>

    @FormUrlEncoded
    @POST("send_otp.php")
    fun loginWithOtp(
        @Field("mobile_number") mobileNumber: String
    ): Call<OtpResponse>

    @FormUrlEncoded
    @POST("verify_otp.php")
    fun verifyOtp(
        @Field("mobile_number") mobileNumber: String,
        @Field("otp") otp: String
    ): Call<UserDetails>



}