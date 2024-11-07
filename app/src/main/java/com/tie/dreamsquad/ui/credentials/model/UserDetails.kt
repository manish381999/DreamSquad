package com.tie.dreamsquad.ui.credentials.model

data class UserDetails(
    val status: String,
    val message: String,
    val user: User?
)

data class User(
    val id: Int,
    val mobile_number: String,
    val name: String?,
    val email_id: String?,
    val profile_pic: String?,
    val is_verified: Int,
    val status: Int,
    val deleted: Int,
    val created_at: String
)

