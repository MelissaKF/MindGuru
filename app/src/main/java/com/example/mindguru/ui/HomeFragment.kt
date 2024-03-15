package com.example.mindguru.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentHomeBinding
import com.example.mindguru.ui.adapter.CategoryAdapter
import com.example.mindguru.ui.adapter.DifficultyAdapter
import kotlinx.coroutines.launch

class HomeFragment : Fragment() {

    private lateinit var binding: FragmentHomeBinding
    private val viewModel: MainViewModel by activityViewModels()
    private var availableCategories: List<String> = emptyList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.categories.observe(viewLifecycleOwner) { categories ->
            val categoryAdapter = CategoryAdapter(categories) { categoryId ->
                viewModel.updateSelectedCategoryId(categoryId)

                val selectedCategory = categories.find { it.id == categoryId }

                selectedCategory?.let {
                    binding.selectedCategoryTV.text = it.name
                    binding.selectedCategoryTV.visibility = View.VISIBLE
                    binding.recyclerViewCategories.visibility = View.GONE
                    binding.recyclerViewDifficulty.visibility = View.VISIBLE

                    binding.difficultyTV.visibility = View.VISIBLE

                    val paramsSelectedCategoryCV =
                        binding.selectedCategoryCV.layoutParams as ConstraintLayout.LayoutParams
                    paramsSelectedCategoryCV.topToBottom = R.id.categoryTV
                    binding.selectedCategoryCV.layoutParams = paramsSelectedCategoryCV

                    val paramsDifficultyTV =
                        binding.difficultyTV.layoutParams as ConstraintLayout.LayoutParams
                    paramsDifficultyTV.topToBottom = R.id.selectedCategoryCV
                    binding.difficultyTV.layoutParams = paramsDifficultyTV

                }
            }
            val layoutManager = LinearLayoutManager(context, LinearLayoutManager.VERTICAL, false)
            binding.recyclerViewCategories.layoutManager = layoutManager

            binding.recyclerViewCategories.adapter = categoryAdapter


            viewModel.selectedCategoryId.observe(viewLifecycleOwner) { categoryId ->
                val selectedCategory = viewModel.categories.value?.find { it.id == categoryId }
                selectedCategory?.let {
                    binding.selectedCategoryTV.visibility = View.VISIBLE
                    binding.selectedCategoryTV.text = it.name
                    binding.recyclerViewCategories.visibility = View.GONE
                }
            }

            binding.selectedCategoryTV.setOnClickListener {
                binding.selectedCategoryTV.visibility = View.GONE
                binding.recyclerViewCategories.visibility = View.VISIBLE
                binding.difficultyTV.visibility = View.GONE
                binding.recyclerViewDifficulty.visibility = View.GONE

                val paramsDifficultyTV =
                    binding.difficultyTV.layoutParams as ConstraintLayout.LayoutParams
                paramsDifficultyTV.topToBottom = R.id.recyclerViewCategories
                binding.difficultyTV.layoutParams = paramsDifficultyTV
            }
        }

        viewModel.loadDifficultyLevels()

        viewModel.difficultyLevels.observe(viewLifecycleOwner) { levels ->
            val difficultyAdapter = DifficultyAdapter(levels) { selectedDifficulty ->

                viewModel.updateSelectedDifficulty(selectedDifficulty)
                Log.d("selected Difficulty", "Selected Difficulty: $selectedDifficulty")

                viewModel.selectedCategoryId.value?.let { categoryId ->
                    viewModel.selectedDifficulty.value?.let { selectedDifficulty ->
                        binding.recyclerViewDifficulty.visibility = View.GONE
                        binding.selectedDifficultyTV.text = selectedDifficulty
                        binding.selectedDifficultyTV.visibility = View.VISIBLE


                        viewModel.fetchTriviaQuestionsByCategoryAndDifficulty(
                            categoryId,
                            selectedDifficulty
                        ) { success ->
                            if (success) {
                                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToPlayFieldFragment())
                            } else {
                                Log.e(
                                    "fetchTriviaQuestions",
                                    "Fragen konnten nicht geladen werden."
                                )
                            }
                        }
                    }
                }
            }
            binding.recyclerViewDifficulty.adapter = difficultyAdapter
        }
        binding.logoutButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                viewModel.logout()
                findNavController().navigate(HomeFragmentDirections.actionHomeFragmentToWelcomeFragment())
            }
        }

        binding.exitButton.setOnClickListener {
            viewModel.viewModelScope.launch{
                viewModel.logout()
                requireActivity().finish()
            }
        }
    }

    override fun onPause() {
        super.onPause()
        binding.recyclerViewCategories.visibility = View.VISIBLE
        binding.selectedCategoryTV.visibility = View.GONE
        binding.recyclerViewDifficulty.visibility = View.GONE
        binding.difficultyTV.visibility = View.GONE
    }

    override fun onResume() {
        super.onResume()
        binding.recyclerViewCategories.visibility = View.VISIBLE
        binding.selectedCategoryTV.visibility = View.GONE
        binding.recyclerViewDifficulty.visibility = View.GONE
        binding.difficultyTV.visibility = View.GONE
    }
}