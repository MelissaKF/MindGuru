package com.example.mindguru.remote.model

import com.squareup.moshi.Json

data class SessionTokenResponse(
    @Json(name = "token")
    val token: String
)