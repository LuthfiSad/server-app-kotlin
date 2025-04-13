package com.example.serverapp.data.model

data class VerificationResult(
    val success: Boolean,
    val message: String,
    val verified: Boolean = false,
    val method: String? = null
)