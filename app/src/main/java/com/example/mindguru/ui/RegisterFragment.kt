package com.example.mindguru.ui

import AppDatabase
import UserEntity
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.mindguru.R
import com.example.mindguru.Repository.UserRepository
import com.example.mindguru.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private val userDao = AppDatabase.getInstance(requireContext()).userDao()
    private lateinit var userRepository: UserRepository
    private val viewModel: ViewModel by viewModels {
        ViewModelFactory(userRepository)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Hier die userRepository initialisieren
        userRepository = UserRepository(userDao)

        binding.registerButton.setOnClickListener {
            val username = binding.usernameEditText.text.toString()
            val password = binding.passwordEditText.text.toString()

            if (isValidCredentials(username, password)) {
                viewModel.viewModelScope.launch {
                    val existingUser = userRepository.getUserByUsername(username)

                    if (existingUser == null) {
                        val newUser = UserEntity(username = username, password = password)

                        viewModel.viewModelScope.launch {
                            userRepository.insertUser(newUser)
                        }

                        findNavController().navigate(R.id.action_registerFragment_to_profileFragment)
                    } else {
                        Toast.makeText(requireContext(), "Benutzer existiert bereits, bitte einloggen!", Toast.LENGTH_LONG).show()
                    }
                }
            } else {
                // Benutzereingabe ist ungültig
                // Hier könntest du eine Fehlermeldung anzeigen
            }
        }
    }

    private fun isValidCredentials(username: String, password: String): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
}