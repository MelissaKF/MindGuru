package com.example.mindguru.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TriviaResponse(
    @Json(name = "results")
    val results: List<TriviaQuestionResponse>
)