package com.example.mindguru.remote.model

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TriviaQuestionResponse(
    val question: String,
    @Json(name = "correct_answer")
    val correctAnswer: String,
    @Json(name = "incorrect_answers")
    val incorrectAnswers: List<String>
)