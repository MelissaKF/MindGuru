package com.example.mindguru.model

data class Question(
    val questionId : String? = null,
    val questionText: String? = null,
    val options: Map<String, Options>? = null,
    val points: Int? = null
) {

    companion object {
        fun fromMap(map: Map<String, Any>?): Question {
            val optionAMap = map?.get("OptionA") as? Map<*, *>
            val optionA = Options(
                text = optionAMap?.get("text") as? String,
                isCorrect = optionAMap?.get("isCorrect") as Boolean
            )

            val optionBMap = map?.get("OptionB") as? Map<*, *>
            val optionB = Options(
                text = optionBMap?.get("text") as? String,
                isCorrect = optionBMap?.get("isCorrect") as Boolean
            )
            val optionCMap = map?.get("OptionC") as? Map<*, *>
            val optionC = Options(
                text = optionCMap?.get("text") as? String,
                isCorrect = optionCMap?.get("isCorrect") as Boolean
            )
            val optionDMap = map?.get("OptionB") as? Map<*, *>
            val optionD = Options(
                text = optionDMap?.get("text") as? String,
                isCorrect = optionDMap?.get("isCorrect") as Boolean
            )

            val optionsMap = mapOf(
                "OptionA" to optionA,
                "OptionB" to optionB,
                "OptionC" to optionC,
                "OptionD" to optionD,

            )

            return Question(
                questionId = map?.get("questionId") as? String,
                questionText = map?.get("questionText") as? String,
                options = optionsMap,
                points = map?.get("points") as? Int
            )
        }
    }
}
