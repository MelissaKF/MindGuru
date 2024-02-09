package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(layoutInflater, container, false)
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.loginButton.setOnClickListener {
            viewModel.login(
                binding.editTextEmail.text.toString(),
                binding.editTextPassword.text.toString()
            )
            findNavController().navigate(LoginFragmentDirections.actionLoginFragmentToPlayFieldFragment())
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Sicherstellung, dass die Bottom Navigation wieder sichtbar wird, wenn das Fragment zerstört wird
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.VISIBLE
    }

}