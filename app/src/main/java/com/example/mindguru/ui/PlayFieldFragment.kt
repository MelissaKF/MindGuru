package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.navigation.fragment.findNavController
import com.example.mindguru.databinding.FragmentPlayFieldBinding

class PlayFieldFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentPlayFieldBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayFieldBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.username.observe(viewLifecycleOwner, Observer { username ->
            username?.let {
                binding.textViewUsername.text = "Benutzername: $username"
            }
        })

        viewModel.userPoints.observe(viewLifecycleOwner, Observer { points ->
            points?.let {
                binding.textViewPoints.text = "Punkte: $points"
            }
        })

        binding.PointCountBtn.setOnClickListener {
            viewModel.addUserPoints(25)
        }

        binding.logoutBtn.setOnClickListener {
            viewModel.logout()
            findNavController().navigate(PlayFieldFragmentDirections.actionPlayFieldFragmentToWelcomeFragment())
        }
    }
}
