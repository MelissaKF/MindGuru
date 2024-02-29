package com.example.mindguru.remote.model

data class QuestionResponse(
    val type: String?,
    val difficulty: String?,
    val category: String?,
    val question: String?,
    val correctAnswer: String?,
    val incorrectAnswers: List<String>?
)