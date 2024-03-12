package com.example.mindguru.remote

import com.example.mindguru.remote.model.SessionTokenResetResponse
import com.example.mindguru.remote.model.SessionTokenResponse
import com.example.mindguru.remote.model.TriviaCategoriesResponse
import com.squareup.moshi.Moshi
import com.squareup.moshi.kotlin.reflect.KotlinJsonAdapterFactory
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import retrofit2.http.GET
import retrofit2.http.Query

private val moshi = Moshi.Builder()
    .add(KotlinJsonAdapterFactory())
    .build()

private val retrofit = Retrofit.Builder()
    .baseUrl("https://opentdb.com/")
    .addConverterFactory(MoshiConverterFactory.create(moshi))
    .build()

interface TriviaApiService {

    @GET("api_token.php")
    suspend fun requestSessionToken(
        @Query("command") command: String
    ): SessionTokenResponse

    @GET("api.php")
    suspend fun getTriviaQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String,
        @Query("encode") encode: String,
        @Query("token") token: String
    ): TriviaResponse

    @GET("api_category.php")
    suspend fun getTriviaCategories(): TriviaCategoriesResponse

    @GET("api_token.php")
    suspend fun resetSessionToken(
        @Query("command") command: String,
        @Query("token") token: String
    ): SessionTokenResetResponse

    object TriviaApi {
        val triviaApiService: TriviaApiService by lazy { retrofit.create(TriviaApiService::class.java) }
    }
}