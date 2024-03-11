package com.example.mindguru.model

import com.squareup.moshi.Json

data class Category(
    @Json(name = "id")
    val id: Int,
    @Json(name = "name")
    val name: String
)