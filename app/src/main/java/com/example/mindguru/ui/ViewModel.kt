package com.example.mindguru.ui


import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.mindguru.Repository.UserRepository
import com.example.mindguru.data.User

class ViewModel(private val userRepository: UserRepository) : ViewModel() {

    private val _user = MutableLiveData<User>()
    val user: LiveData<User>
        get() = _user

    fun setUserName(name: String) {
        val currentUser = _user.value ?: User(name = "", password = "")
        val updatedUser = currentUser.copy(name = name)
        _user.value = updatedUser
    }

    fun setUserPassword(password: String) {
        val currentUser = _user.value ?: User("", "")
        val updatedUser = currentUser.copy(password = password)
        _user.value = updatedUser
    }
}