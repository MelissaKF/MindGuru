package com.example.mindguru.ui

import android.text.Html
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindguru.data.DatasourceDifficultyString
import com.example.mindguru.model.Category
import com.example.mindguru.model.Difficulty
import com.example.mindguru.model.Option
import com.example.mindguru.model.Profile
import com.example.mindguru.model.Question
import com.example.mindguru.remote.model.TriviaResponse
import com.example.mindguru.repository.TriviaRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthException
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.UserProfileChangeRequest
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

    // region LiveData Firebase Handling

    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user

    private val _userPoints: MutableLiveData<Int> = MutableLiveData()
    val userPoints: LiveData<Int>
        get() = _userPoints

    private val _loginStatus: MutableLiveData<LoginStatus> = MutableLiveData()
    val loginStatus: LiveData<LoginStatus>
        get() = _loginStatus

    private val _username: MutableLiveData<String?> = MutableLiveData()
    val username: LiveData<String?>
        get() = _username

    //endregion

    //region LiveData API Handling

    private val _selectedCategoryId: MutableLiveData<Int> = MutableLiveData()
    val selectedCategoryId: LiveData<Int>
        get() = _selectedCategoryId

    private val _questions: MutableLiveData<List<Question?>> = MutableLiveData()
    val questions: MutableLiveData<List<Question?>>
        get() = _questions

    private val _categories: MutableLiveData<List<Category>> = MutableLiveData()
    val categories: LiveData<List<Category>>
        get() = _categories

    private val _difficultyLevels: MutableLiveData<List<Difficulty>> = MutableLiveData()
    val difficultyLevels: LiveData<List<Difficulty>>
        get() = _difficultyLevels

    private val _selectedDifficulty: MutableLiveData<String> = MutableLiveData()
    val selectedDifficulty: LiveData<String>
        get() = _selectedDifficulty

    private val _currentQuestion: MutableLiveData<Question?> = MutableLiveData()
    val currentQuestion: MutableLiveData<Question?>
        get() = _currentQuestion

    private val _currentQuestionPoints: MutableLiveData<Question?> = MutableLiveData()
    val currentQuestionPoints: MutableLiveData<Question?>
        get() = _currentQuestionPoints

    // endregion

    // region Functions for Firebase Handling

    private fun setupUserEnv() {
        auth.currentUser?.let { firebaseUser ->
            Log.d("Firebase", "Current User UID: ${firebaseUser.uid}")
            profileRef = firestore.collection("user").document(firebaseUser.uid)
            Log.d("Firebase", "profileRef initialized: $profileRef")

            profileRef.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    Log.e("Firebase", "Error fetching user data: $error")
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val points = snapshot.getLong("userPoints")?.toInt() ?: 0
                    _userPoints.postValue(points)

                    val username = snapshot.getString("username")
                    _user.postValue(firebaseUser)
                    _username.postValue(username)
                }
            }
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

    fun updateUserData(username: String) {
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            user.updateProfile(
                UserProfileChangeRequest.Builder()
                    .setDisplayName(username)
                    .build()
            )
                .addOnSuccessListener {
                    profileRef.update("username", username)
                        .addOnSuccessListener {
                        }
                        .addOnFailureListener { e ->
                            Log.e("Firebase", "Failed to update username in database: $e")
                        }
                }
                .addOnFailureListener { e ->
                    Log.e("MainViewModel", "Failed to update username: $e")
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

    // endregion

    // region Functions for API Handling

    private fun fetchSessionToken() {
        viewModelScope.launch {
            try {
                val response = triviaRepository.getSessionToken()
                sessionToken = response.token
                Log.d("SessionToken", "Received Session Token: $sessionToken")
            } catch (e: Exception) {
                Log.e("SessionToken", "Error fetching Session Token", e)
            }
        }
    }

    fun fetchTriviaQuestionsByCategoryAndDifficulty(categoryId: Int, difficulty: String, callback: (Boolean) -> Unit) {
        viewModelScope.launch {
            try {
                val response = triviaRepository.getTriviaQuestions(
                    amount = 10,
                    category = categoryId,
                    difficulty = difficulty,
                    type = "multiple",
                    encode = "",
                    token = sessionToken
                )
                handleTriviaResponse(response)
                callback(true)
            } catch (e: Exception) {
                Log.e("API", "Error fetching trivia questions", e)
                callback(false)
            }
        }
    }

    fun fetchFilteredTriviaCategories() {
        viewModelScope.launch {
            try {
                val response = triviaRepository.getTriviaCategories()
                val filteredCategories = mutableListOf<Category>()

                for (category in response.triviaCategories) {
                    val categoryCountResponse =
                        triviaRepository.checkCategoryQuestionCount(category.id)
                    if (categoryCountResponse.triviaCategoryQuestionCount.totalQuestionCount > 200) {
                        filteredCategories.add(category)
                    }
                }
                _categories.postValue(filteredCategories)
            } catch (e: Exception) {
                Log.e("API", "Error fetching trivia categories", e)
            }
        }
    }

    //endregion

    //region Functions to prepare API Response for UI

    private fun handleTriviaResponse(response: TriviaResponse) {
        response.results.let { results ->
            val decodedQuestions = results.map { result ->
                val shuffledOptions = shuffleOptions(result.correctAnswer, result.incorrectAnswers)
                val options = shuffledOptions.map { Option(it.text, it.correctAnswer) }

                Question.createQuestion(result.question, options)
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

        val shuffledOptions = allOptions.shuffled().map {
            Option(decodeHtmlString(it), it == correctAnswer)
        }

        return shuffledOptions
    }

    private fun decodeHtmlString(htmlString: String): String {
        return Html.fromHtml(htmlString, Html.FROM_HTML_MODE_LEGACY).toString()
    }

    fun updateSelectedCategoryId(categoryId: Int) {
        _selectedCategoryId.value = categoryId
    }

    fun updateSelectedDifficulty(difficulty: String) {
        _selectedDifficulty.value = difficulty
    }

    fun loadDifficultyLevels() {
        val dataSource = DatasourceDifficultyString()
        val levels = dataSource.loadData()
        _difficultyLevels.value = levels
    }

    // endregion
}