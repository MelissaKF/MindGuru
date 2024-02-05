package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentLoginBinding

class LoginFragment : Fragment() {

    private lateinit var binding : FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentLoginBinding.inflate(layoutInflater,container,false)
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
    }

    override fun onDestroyView() {
        super.onDestroyView()

        // Stelle sicher, dass die Bottom Navigation wieder sichtbar wird, wenn das Fragment zerstört wird
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.VISIBLE
    }
}