package com.example.mindguru.remote.model

import com.squareup.moshi.Json

data class TriviaCategoryQuestionCount(
    @Json(name = "total_question_count") val totalQuestionCount: Int,
    @Json(name = "total_easy_question_count") val totalEasyQuestionCount: Int,
    @Json(name = "total_medium_question_count") val totalMediumQuestionCount: Int,
    @Json(name = "total_hard_question_count") val totalHardQuestionCount: Int
)