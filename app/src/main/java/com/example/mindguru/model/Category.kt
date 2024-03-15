package com.example.mindguru.model

data class Category(
    val id: Int,
    val name: String,
    var isVisible : Boolean = true,
)