package com.example.mindguru.remote.model

import com.example.mindguru.model.Category
import com.squareup.moshi.Json

data class TriviaCategoriesResponse(
    @Json(name = "trivia_categories")
    val triviaCategories: List<Category>
)