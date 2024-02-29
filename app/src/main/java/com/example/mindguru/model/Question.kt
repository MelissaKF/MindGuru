package com.example.mindguru.model

data class Question(
    val question: String,
    val options: List<Option>
) {

    companion object {
        const val points: Int = 25
    }
}