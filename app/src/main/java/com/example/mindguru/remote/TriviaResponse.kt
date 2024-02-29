package com.example.mindguru.remote

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

@JsonClass(generateAdapter = true)
data class TriviaResponse(
    @Json(name = "results")
    val results: List<TriviaQuestion>
)

@JsonClass(generateAdapter = true)
data class TriviaQuestion(
    @Json(name = "question")
    val question: String,
    @Json(name = "correct_answer")
    val correctAnswer: String,
    @Json(name = "incorrect_answers")
    val incorrectAnswers: List<String>
)