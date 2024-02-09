package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentRegisterBinding

class RegisterFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentRegisterBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRegisterBinding.inflate(inflater, container, false)
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val navController = findNavController()

        binding.cancelButton.setOnClickListener {
            navController.navigate(RegisterFragmentDirections.actionRegisterFragmentToWelcomeFragment())
        }

        binding.registerButton.setOnClickListener {
            viewModel.register(
                binding.usernameEditText.text.toString(),
                binding.EmailEditText.text.toString(),
                binding.passwordEditText.text.toString()
            )

            viewModel.user.observe(viewLifecycleOwner) {
                if (it != null) {
                    Toast.makeText(
                        requireContext(),
                        "Account erfolgreich erstellt!\nViel Vergnügen beim Raten!",
                        Toast.LENGTH_LONG
                    ).show()

                    findNavController().navigate(RegisterFragmentDirections.actionRegisterFragmentToPlayFieldFragment())
                }
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Sicherstellung, dass die Bottom Navigation wieder sichtbar wird, wenn das Fragment zerstört wird
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.VISIBLE
    }
}