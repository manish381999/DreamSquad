package com.tie.dreamsquad.ui.credentials.model

// Image data model
data class ImageData(
    val image_name: String,
    val image_url: String
)

// Response model for the entire JSON response
data class ImageResponse(
    val status: String,
    val images: List<ImageData>
)