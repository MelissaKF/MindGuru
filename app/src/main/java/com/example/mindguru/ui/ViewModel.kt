package com.example.mindguru.ui


import UserEntity
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.mindguru.Repository.UserRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ViewModel(private val userRepository: UserRepository) : ViewModel() {

    fun registerUser(username: String, password: String) {
        viewModelScope.launch(Dispatchers.IO) {
            // Überprüfen, ob der Benutzer bereits existiert
            val existingUser = userRepository.getUserByUsername(username)

            if (existingUser == null) {
                // Der Benutzer existiert nicht, also fügen Sie ihn zur Datenbank hinzu
                val newUser = UserEntity(username = username, password = password)
                userRepository.insertUser(newUser)
            } else {
                // Der Benutzer existiert bereits, hier könnten Sie eine geeignete Benachrichtigung senden
            }
        }
    }
}