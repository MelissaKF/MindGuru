package com.example.mindguru.ui

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.LinearSnapHelper
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentHomeBinding
import com.example.mindguru.ui.adapter.CategoryAdapter

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MainViewModel by activityViewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.GONE
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val adapter = CategoryAdapter(categories) { categoryId ->
                viewModel.updateSelectedCategoryId(categoryId)
                viewModel.fetchTriviaQuestionsByCategory(categoryId)

            findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPlayFieldFragment())
            }
            binding.recyclerViewCategories.adapter = adapter

            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.recyclerViewCategories.layoutManager = layoutManager

            val snapHelper = LinearSnapHelper()
            snapHelper.attachToRecyclerView(binding.recyclerViewCategories)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        val bottomNav = requireActivity().findViewById<View>(R.id.bottomNavMenu)
        bottomNav.visibility = View.VISIBLE
    }
}