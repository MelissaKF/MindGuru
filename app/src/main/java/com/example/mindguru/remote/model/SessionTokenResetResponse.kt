package com.example.mindguru.remote.model

import com.squareup.moshi.Json

data class SessionTokenResetResponse(
    @Json(name = "response_code")
    val responseCode: Int
)