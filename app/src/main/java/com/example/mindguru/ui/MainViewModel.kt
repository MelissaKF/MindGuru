package com.example.mindguru.ui

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mindguru.model.Profile
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage

class MainViewModel : ViewModel() {

    val auth = Firebase.auth
    val firestore = Firebase.firestore
    val storage = Firebase.storage

    private val _user: MutableLiveData<FirebaseUser?> = MutableLiveData()
    val user: LiveData<FirebaseUser?>
        get() = _user

    private val _userPoints: MutableLiveData<Int> = MutableLiveData()
    val userPoints: LiveData<Int>
        get() = _userPoints

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

    fun addUserPoints(pointsToAdd: Int) {
        val currentUser = auth.currentUser
        Log.d("Firebase", "Current User: $currentUser")
        currentUser?.let { user ->
            profileRef.update("userPoints", FieldValue.increment(pointsToAdd.toLong()))
                .addOnSuccessListener {
                    Log.d("Points", "$pointsToAdd")
                    _userPoints.postValue(_userPoints.value?.plus(pointsToAdd))
                }
                .addOnFailureListener { e ->
                    Log.e("Points", "$e")
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
}
