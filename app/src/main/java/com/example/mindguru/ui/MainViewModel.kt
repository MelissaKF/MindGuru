package com.example.mindguru.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mindguru.model.Options
import com.example.mindguru.model.Profile
import com.example.mindguru.model.Question
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class MainViewModel : ViewModel() {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore
    private val questionsCollection = firestore.collection("Questions")
    private val storage = Firebase.storage

    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user

    private val _userPoints: MutableLiveData<Int?> = MutableLiveData()
    val userPoints: LiveData<Int?>
        get() = _userPoints

    private val _currentPoints: MutableLiveData<Int> = MutableLiveData()
    val currentPoints: LiveData<Int>
        get() = _currentPoints

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

    fun fetchQuestionFromFirestore() {
        val questionId = "question1"
        val questionDocumentRef = questionsCollection.document(questionId)

        questionDocumentRef.addSnapshotListener { snapshot, error ->
            if (error != null) {
                // Behandle den Fehler hier
                return@addSnapshotListener
            }

            if (snapshot != null && snapshot.exists()) {
                val questionText = snapshot.getString("questionText")
                val optionsMap = if (snapshot.contains("options")) {
                    snapshot.get("options") as? Map<String, Map<String, Any>>
                } else {
                    null
                }

                val options = optionsMap?.mapValues { entry ->
                    val optionMap = entry.value
                    val text = optionMap["text"] as String
                    val isCorrect = optionMap["isCorrect"] as Boolean
                    Options(text, isCorrect)
                }

                val question = Question(questionId, questionText, options)
                _currentQuestion.postValue(question)

                val points = snapshot.getLong("points")?.toInt() ?: 0
                Log.d("Firestore", "Points retrieved from Firestore: $points")
                _currentPoints.postValue(points)

                Log.d("Firestore", "Question fetched successfully: $question")
            }
        }
    }

    private fun addUserPointsForQuestion(
        questionId: String,
        selectedOptionId: String,
        pointsToAdd: Int
    ) {
        Log.d("PointsAdd", "Betrete die Funktion addUserPointsForQuestion")
        val currentUser = auth.currentUser
        currentUser?.let { user ->
            val questionRef = questionsCollection.document(questionId)

            questionRef.get()
                .addOnSuccessListener { documentSnapshot ->
                    Log.d("PointsAdd", "Innerhalb des addOnSuccessListener-Blocks")
                    if (documentSnapshot.exists()) {
                        val question = documentSnapshot.toObject(Question::class.java)
                        question?.let {
                            val selectedOption = question.options?.get(selectedOptionId)
                            Log.d("PointsAdd", "Selected Option: $selectedOption")
                            if (selectedOption?.isCorrect == true) {
                                Log.d("PointsAdd", "Vor dem Aufruf der Funktion addPointsToUser")
                                // Punkte zum Benutzerkonto hinzufügen
                                addPointsToUser(user.uid, pointsToAdd)
                            }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    Log.e("Points", "Fehler beim Abrufen der Frage: $e")
                }
        }
    }

    private fun addPointsToUser(userId: String, pointsToAdd: Int) {
        Log.d("PointsAdd", "Betrete die Funktion addPointsToUser")
        val userDocRef = firestore.collection("user").document(userId)

        // Füge die Punkte zum Benutzerkonto hinzu
        firestore.runTransaction { transaction ->
            val userDoc = transaction.get(userDocRef)
            val currentPoints = userDoc.getLong("userPoints") ?: 0
            transaction.update(userDocRef, "userPoints", currentPoints + pointsToAdd)
        }
            .addOnSuccessListener {
                Log.d("PointsAdd", "$pointsToAdd Punkte wurden dem Benutzer hinzugefügt.")
                _userPoints.postValue(_userPoints.value?.plus(pointsToAdd))
            }
            .addOnFailureListener { e ->
                Log.e("Points", "Fehler beim Hinzufügen von Punkten zum Benutzer: $e")
            }
        Log.d("PointsAdd", "Ende der Funktion addPointsToUser")
    }

    // Funktion, um die Antwort zu überprüfen
    fun checkAnswer(selectedOptionId: String): Boolean {
        Log.d("PointsAdd", "Betrete die Funktion checkAnswer")
        val currentQuestion = _currentQuestion.value

        // Überprüfung, ob currentQuestion und options nicht null sind
        if (currentQuestion?.options != null) {
            // Finde die ausgewählte Option in der Map der Optionen

            val selectedOption = currentQuestion.options[selectedOptionId]

            // Überprüfung, ob die ausgewählte Option nicht null ist und isCorrect true ist
            val isCorrect = selectedOption?.isCorrect == true

            Log.d("PointsAdd", "Ist die Antwort korrekt: $isCorrect")
            Log.d("Points", "Benutzerpunkte vorher: ${_userPoints.value}")

            // Falls die Antwort korrekt ist, füge die Punkte hinzu
            if (isCorrect) {
                val points = currentQuestion.points ?: 0
                Log.d("PointsAdd", "Vor dem Aufruf der Funktion addUserPointsForQuestion")
                addUserPointsForQuestion(currentQuestion.questionId!!, selectedOptionId, points)
                Log.d("PointsAdd", "Nach dem Aufruf der Funktion addUserPointsForQuestion")
                _currentPoints.postValue(_currentPoints.value?.plus(points))
                Log.d("PointsAdd", "Aktuelle Punkte: ${_currentPoints.value}")
            }

            Log.d("Points", "Benutzerpunkte nachher: ${_userPoints.value}")

            return isCorrect
        }

        return false
    }

    fun getCurrentPoints(): Int? {
        return _currentPoints.value ?: 0
    }

    fun getUserPoints(): Int? {
        return _userPoints.value ?: 0
    }
}