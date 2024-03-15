package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mindguru.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        binding.cancelButton.setOnClickListener {
            navController.navigate(RegisterFragmentDirections.actionRegisterFragmentToWelcomeFragment())
        }

        binding.registerButton.setOnClickListener {
            val password1 = binding.passwordEditText1.text.toString()
            val password2 = binding.passwordEditText2.text.toString()

            if (password1 == password2) {
                viewModel.register(
                    binding.usernameEditText.text.toString(),
                    binding.emailEditText.text.toString(),
                    password1
                )

                viewModel.user.observe(viewLifecycleOwner) {
                    if (it != null) {
                        Toast.makeText(
                            requireContext(),
                            "Account erfolgreich erstellt!\nViel Vergnügen beim Raten!",
                            Toast.LENGTH_LONG
                        ).show()

                        findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToHomeFragment())
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    "The passwords entered do not match.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }
}