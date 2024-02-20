package com.example.mindguru.model

data class Options(
    val text: String? = "",
    val isCorrect: Boolean? = null
) {

    constructor() : this("", null)
}