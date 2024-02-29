package com.example.mindguru.remote
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
    @GET("api.php?")
    suspend fun getTriviaQuestions(
        @Query("amount") amount: Int,
        @Query("category") category: Int,
        @Query("difficulty") difficulty: String,
        @Query("type") type: String,
        @Query("encode") encode : String
    ): TriviaResponse
}

//https://opentdb.com/api.php?amount=50&difficulty=easy&type=multiple&encode=url3986