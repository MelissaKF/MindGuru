package com.example.mindguru.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mindguru.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        viewModel.fetchFilteredTriviaCategories()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            viewModel.login(
                binding.editTextEmail.text.toString(),
                binding.editTextPassword.text.toString()
            )
        }

        viewModel.loginStatus.observe(viewLifecycleOwner) { loginStatus ->
            when (loginStatus!!) {
                MainViewModel.LoginStatus.SUCCESS -> {
                    Log.d("LoginStatusCategories", viewModel.categories.value.toString())
                    findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToHomeFragment())
                }

                MainViewModel.LoginStatus.EMAIL_NOT_FOUND -> showToast("EMail not found")
                MainViewModel.LoginStatus.WRONG_PASSWORD -> showToast("Wrong password")
                MainViewModel.LoginStatus.FAILURE -> showToast("User not found")
            }
        }

        binding.registerButton.setOnClickListener {
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToRegisterFragment())
        }
    }


    private fun showToast(message: String) {
       Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show()
    }
}