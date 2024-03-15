package com.example.mindguru.remote.model

import com.squareup.moshi.Json

data class TriviaCategoryCountResponse(
    @Json(name ="category_id") val categoryId: Int,
    @Json(name = "category_question_count") val triviaCategoryQuestionCount: TriviaCategoryQuestionCount
)
