package com.example.mindguru.ui

import androidx.lifecycle.ViewModelProvider
import com.example.mindguru.Repository.UserRepository

class ViewModelFactory(private val userRepository: UserRepository) : ViewModelProvider.Factory {
    override fun <T : androidx.lifecycle.ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(ViewModel::class.java)) {
            return ViewModel(userRepository) as T
        }
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}