package com.example.serverapp.data.model

data class VerificationRequest(
    val code: String,
    val method: String? = null,
    val data: String? = null
)