package com.example.mindguru.model

import android.text.Html
import android.text.Spanned

data class Question(
    val question: Spanned,
    val options: List<Option>,
) {
    companion object {
        const val currentPointsEasy: Int = 25

        fun createQuestion(questionText: String, options: List<Option>): Question {
            val decodedQuestion = decodeHtmlString(questionText)
            return Question(decodedQuestion, options)
        }
        private fun decodeHtmlString(htmlString: String): Spanned {
            return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY)
        }
    }
}