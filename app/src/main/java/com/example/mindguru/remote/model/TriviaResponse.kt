package com.example.mindguru.remote.model

import com.squareup.moshi.Json

data class TriviaResponse(
    @Json(name = "response_code")
    val responseCode: Int,
    val results: List<QuestionResponse>?
)
