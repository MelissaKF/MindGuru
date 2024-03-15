package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mindguru.databinding.FragmentProfileBinding
import com.google.firebase.auth.FirebaseUser


class ProfileFragment : Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.user.observe(viewLifecycleOwner) { user ->
            user?.let { displayUserData(it) }
        }

        viewModel.username.observe(viewLifecycleOwner) { username ->
            username?.let { binding.usernameEditText.setText(it) }
        }

        binding.registerButton.setOnClickListener {
            val newUsername = binding.usernameEditText.text.toString()
            viewModel.updateUserData(newUsername)
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToHomeFragment())
        }

        binding.cancelButton.setOnClickListener {
            findNavController().navigate(ProfileFragmentDirections.actionProfileFragmentToHomeFragment())
        }
    }

    private fun displayUserData(user: FirebaseUser) {
        binding.usernameEditText.setText(viewModel.username.value ?: "")
    }
}