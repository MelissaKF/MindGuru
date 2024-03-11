package com.example.mindguru.ui

import android.text.Html
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindguru.model.Category
import com.example.mindguru.model.Option
import com.example.mindguru.model.Profile
import com.example.mindguru.model.Question
import com.example.mindguru.remote.TriviaResponse
import com.example.mindguru.repository.TriviaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainViewModel : ViewModel() {

    enum class LoginStatus {
        SUCCESS,
        EMAIL_NOT_FOUND,
        WRONG_PASSWORD,
        FAILURE
    }

    private lateinit var profileRef: DocumentReference
    private val triviaRepository = TriviaRepository()
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()
    private var sessionToken: String = ""

    init {
        fetchSessionToken()
        setupUserEnv()
    }

    // region LiveData
    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user

    private val _userPoints: MutableLiveData<Int> = MutableLiveData()
    val userPoints: LiveData<Int>
        get() = _userPoints

    private val _loginStatus: MutableLiveData<LoginStatus> = MutableLiveData()
    val loginStatus: LiveData<LoginStatus>
        get() = _loginStatus

    private val _questions: MutableLiveData<List<Question?>> = MutableLiveData()
    val questions: MutableLiveData<List<Question?>>
        get() = _questions

    private val _categories: MutableLiveData<List<Category>> = MutableLiveData()
    val categories: LiveData<List<Category>>
        get() = _categories

    private val _currentQuestion: MutableLiveData<Question?> = MutableLiveData()
    val currentQuestion: MutableLiveData<Question?>
        get() = _currentQuestion

    private val _currentQuestionPoints: MutableLiveData<Question?> = MutableLiveData()
    val currentQuestionPoints: MutableLiveData<Question?>
        get() = _currentQuestionPoints

    private val _username: MutableLiveData<String?> = MutableLiveData()
    val username: LiveData<String?>
        get() = _username
    // endregion

    // region Functions for Firebase handling
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
                setupUserEnv()
                val newProfile = Profile()
                profileRef.set(newProfile)
                addUsername(username)

            } else {
                Log.e("Register", "Registration failed")
            }
        }
    }

    fun login(email: String, password: String) {
        auth.signInWithEmailAndPassword(email, password).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val user = auth.currentUser
                if (user != null) {
                    setupUserEnv()
                    _loginStatus.postValue(LoginStatus.SUCCESS)
                } else {
                    _loginStatus.postValue(LoginStatus.EMAIL_NOT_FOUND)
                }
            } else {
                val errorCode = (task.exception as? FirebaseAuthException)?.errorCode
                when (errorCode) {
                    "ERROR_EMAIL_NOT_FOUND" -> _loginStatus.postValue(LoginStatus.EMAIL_NOT_FOUND)
                    "ERROR_WRONG_PASSWORD" -> _loginStatus.postValue(LoginStatus.WRONG_PASSWORD)
                    else -> {
                        _loginStatus.postValue(LoginStatus.FAILURE)
                        Log.e("Login", "Login failed", task.exception)
                    }
                }
            }
        }
    }
    suspend fun logout() {
        withContext(Dispatchers.IO) {
            Log.d("Logout", "Logging out...")
            auth.signOut()
            _username.postValue("")
            _userPoints.postValue(0)
            setupUserEnv()
            Log.d("Logout", "Logout completed")
        }
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

    fun updateUserPoints(pointsToAdd: Int) {
        _userPoints.value?.let { currentPoints ->
            val newPoints = currentPoints + pointsToAdd
            _userPoints.postValue(newPoints)
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

    // endregion

    // region Functions for API handling
    private fun fetchSessionToken() {
        viewModelScope.launch {
            try {
                val response = triviaRepository.requestSessionToken()
                sessionToken = response.token
                Log.d("SessionToken", "Received Session Token: $sessionToken")
            } catch (e: Exception) {
                Log.e("SessionToken", "Error fetching Session Token", e)
            }
        }
    }

    private fun resetSessionToken() {
        viewModelScope.launch {
            try {
                val response = triviaRepository.resetSessionToken("reset", sessionToken)
                // Handle response if needed
            } catch (e: Exception) {
                Log.e("API", "Error resetting session token", e)
            }
        }
    }

    fun fetchTriviaQuestions() {
        viewModelScope.launch {
            try {
                val response = triviaRepository.getTriviaQuestions(
                    amount = 50,
                    category = 9,
                    difficulty = "easy",
                    type = "multiple",
                    encode = "",
                    token = sessionToken
                )
                handleTriviaResponse(response)
            } catch (e: Exception) {
                Log.e("API", "Error fetching trivia questions", e)
            }
        }
    }

    fun fetchTriviaCategories() {
        viewModelScope.launch {
            try {
                val response = triviaRepository.getTriviaCategories()
                _categories.postValue(response.triviaCategories)
                Log.d("Categories", categories.value.toString())
            } catch (e: Exception) {
                Log.e("API", "Error fetching trivia categories", e)
            }
        }
    }

    private fun handleTriviaResponse(response: TriviaResponse) {
        response.results.let { results ->
            val decodedQuestions = results.map { result ->
                val decodedQuestion = result.question
                val shuffledOptions = shuffleOptions(result.correctAnswer, result.incorrectAnswers)
                val options = shuffledOptions.map { Option(it.text, it.correctAnswer) }
                Question(decodedQuestion, options)
            }
            _questions.postValue(decodedQuestions)
            _currentQuestion.postValue(decodedQuestions.firstOrNull())

            Log.d("API", "Received questions: $decodedQuestions")
        }
    }

    private fun shuffleOptions(
        correctAnswer: String,
        incorrectAnswers: List<String>
    ): List<Option> {
        val allOptions = mutableListOf(correctAnswer)
        allOptions.addAll(incorrectAnswers)

        val shuffledOptions =
            allOptions.shuffled().map { Option(decodeHtmlString(it), it == correctAnswer) }

        return shuffledOptions
    }

    private fun decodeHtmlString(htmlString: String): String {
        return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString()
    }
    // endregion

    fun updateCategories(newCategories: List<Category>) {
        _categories.postValue(newCategories)
    }
}