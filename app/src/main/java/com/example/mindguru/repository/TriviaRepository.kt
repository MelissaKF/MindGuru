package com.example.mindguru.repository

import com.example.mindguru.remote.model.SessionTokenResponse
import com.example.mindguru.remote.model.TriviaCategoriesResponse
import com.example.mindguru.remote.model.TriviaCategoryCountResponse
import com.example.mindguru.remote.model.TriviaResponse
import com.example.mindguru.remote.triviaApiService.TriviaApiService.TriviaApi.triviaApiService

class TriviaRepository() {

    suspend fun getTriviaQuestions(
        amount: Int,
        category: Int,
        difficulty: String,
        type: String,
        encode: String,
        token: String
    ): TriviaResponse {
        return triviaApiService.getTriviaQuestions(amount, category, difficulty, type, encode, token)
    }

    suspend fun getSessionToken(): SessionTokenResponse {
        return triviaApiService.getSessionToken("request")
    }

    suspend fun getTriviaCategories(): TriviaCategoriesResponse {
        return triviaApiService.getTriviaCategories()
    }

    suspend fun checkCategoryQuestionCount(categoryId: Int): TriviaCategoryCountResponse {
        return triviaApiService.checkCategoryQuestionCount(categoryId)
    }
}