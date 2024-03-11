package com.example.mindguru.repository

import com.example.mindguru.remote.TriviaApiService.TriviaApi.triviaApiService
import com.example.mindguru.remote.TriviaResponse
import com.example.mindguru.remote.model.SessionTokenResetResponse
import com.example.mindguru.remote.model.SessionTokenResponse
import com.example.mindguru.remote.model.TriviaCategoriesResponse

class TriviaRepository() {

    suspend fun requestSessionToken(): SessionTokenResponse {
        return triviaApiService.requestSessionToken("request")
    }

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

    suspend fun resetSessionToken(command: String, token: String): SessionTokenResetResponse {
        return triviaApiService.resetSessionToken(command, token)
    }

    suspend fun getTriviaCategories(): TriviaCategoriesResponse {
        return triviaApiService.getTriviaCategories()
    }
}