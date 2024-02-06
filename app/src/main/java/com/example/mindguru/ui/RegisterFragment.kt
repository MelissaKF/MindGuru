package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.mindguru.R
import com.example.mindguru.Repository.UserRepository
import com.example.mindguru.data.UserEntity
import com.example.mindguru.data.database.AppDatabase
import com.example.mindguru.data.database.UserDao
import com.example.mindguru.databinding.FragmentRegisterBinding
import kotlinx.coroutines.launch

class RegisterFragment : Fragment() {

    private lateinit var binding: FragmentRegisterBinding
    private lateinit var userRepository: UserRepository
    private lateinit var userDao: UserDao
    private lateinit var viewModel: ViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)

        userDao = AppDatabase.getInstance(requireContext()).userDao()

        userRepository = UserRepository(userDao)
        val viewModelFactory = ViewModelFactory(userRepository)
        viewModel = ViewModelProvider(this, viewModelFactory).get(ViewModel::class.java)

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()
        userRepository = UserRepository(userDao)

        binding.cancelButton.setOnClickListener {
           navController.navigate(R.id.action_registerFragment_to_welcomeFragment)
        }

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
                Toast.makeText(requireContext(),"Bitte Name und Passwort festlegen!", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun isValidCredentials(username: String, password: String): Boolean {
        return username.isNotBlank() && password.isNotBlank()
    }
}

