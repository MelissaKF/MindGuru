package com.example.mindguru.ui

import com.example.mindguru.remote.TriviaApiService
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindguru.model.Option
import com.example.mindguru.model.Profile
import com.example.mindguru.model.Question
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Retrofit
import retrofit2.converter.moshi.MoshiConverterFactory
import java.net.URLDecoder

class MainViewModel : ViewModel() {

    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    private val triviaApiService: TriviaApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://opentdb.com/")
            .addConverterFactory(MoshiConverterFactory.create())
            .build()
            .create(TriviaApiService::class.java)
    }

    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user

    private val _userPoints: MutableLiveData<Int> = MutableLiveData()
    val userPoints: LiveData<Int>
        get() = _userPoints


    private val _questions: MutableLiveData<List<Question?>> = MutableLiveData()
    val questions: MutableLiveData<List<Question?>>
        get() = _questions

    private val _currentQuestion: MutableLiveData<Question?> = MutableLiveData()
    val currentQuestion: MutableLiveData<Question?>
        get() = _currentQuestion

    private val _username: MutableLiveData<String?> = MutableLiveData()
    val username: LiveData<String?>
        get() = _username

    private lateinit var profileRef: DocumentReference

    init {
        setupUserEnv()
    }

    private fun setupUserEnv() {
        auth.currentUser?.let { firebaseUser ->
            Log.d("Firebase", "Current User UID: ${firebaseUser.uid}")
            profileRef = firestore.collection("user").document(firebaseUser.uid)
            Log.d("Firebase", "profileRef initialized: $profileRef")

            profileRef.get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val points = document.getLong("userPoints")?.toInt() ?: 0
                        _userPoints.postValue(points)

                        val username = document.getString("username")
                        _user.postValue(firebaseUser)
                        _username.postValue(username)
                    }
                }
        }
            ?.addOnFailureListener { exception ->
                Log.e("Firebase", "Error getting user data", exception)
            }
    }

    fun register(username: String, email: String, password: String) {
        auth.createUserWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                // User wurde erstellt
                setupUserEnv()
                val newProfile = Profile()
                profileRef.set(newProfile)
                addUsername(username)

            } else {

            }
        }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener {
            if (it.isSuccessful) {
                setupUserEnv()
            } else {

            }
        }
    }

    fun logout() {
        auth.signOut()
        _username.postValue("")
        _userPoints.postValue(0)
        setupUserEnv()
    }

    private fun addUsername(username: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            profileRef.update("username", username)
                .addOnSuccessListener {
                    Log.d("Username", "Username updated successfully: $username")
                }
                .addOnFailureListener { e ->
                    Log.e("Username", "Failed to update username: $e")
                }
        }
    }

    fun fetchTriviaQuestions() {
        viewModelScope.launch {
            try {
                val encodeType = ""

                val response = withContext(Dispatchers.IO) {
                    triviaApiService.getTriviaQuestions(50, 9, "easy", "multiple", encodeType)
                }

                response.results.let { results ->
                    val decodedQuestions = results.map { result ->
                        val decodedQuestion = result.question

                        val shuffledOptions =
                            shuffleOptions(result.correctAnswer, result.incorrectAnswers)

                        val options = shuffledOptions.map { Option(it.text, it.correctAnswer) }

                        Question(decodedQuestion, options)
                    }

                    _questions.postValue(decodedQuestions)
                    _currentQuestion.postValue(decodedQuestions.firstOrNull())
                    Log.d("API", "Received questions: $decodedQuestions")
                }
            } catch (e: Exception) {
                Log.e("API", "Error fetching trivia questions", e)
            }
        }
    }

    private fun shuffleOptions(
        correctAnswer: String,
        incorrectAnswers: List<String>
    ): List<Option> {
        val allOptions = mutableListOf(correctAnswer)
        allOptions.addAll(incorrectAnswers)
        return allOptions.shuffled().map { Option(it, it == correctAnswer) }
    }

    fun updateUserPoints(pointsToAdd: Int) {
        _userPoints.value?.let { currentPoints ->
            val newPoints = currentPoints + pointsToAdd
            _userPoints.postValue(newPoints)

            // Aktualisiere auch die Punkte in der Firebase-Datenbank
            auth.currentUser?.uid?.let { userId ->
                profileRef.update("userPoints", newPoints)
                    .addOnSuccessListener {
                        Log.d("Firebase", "User points updated successfully: $newPoints")
                    }
                    .addOnFailureListener { e ->
                        Log.e("Firebase", "Failed to update user points: $e")
                    }
            }
        }
    }
}