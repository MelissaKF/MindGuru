package com.example.mindguru.ui

import android.app.AlertDialog
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.ArrayAdapter
import android.widget.ListView
import android.widget.ScrollView
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import androidx.lifecycle.viewModelScope
import androidx.navigation.fragment.findNavController
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentPlayFieldBinding
import com.example.mindguru.model.Category
import com.example.mindguru.model.Question
import com.google.android.material.card.MaterialCardView
import kotlinx.coroutines.launch

class PlayFieldFragment : Fragment() {

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var binding: FragmentPlayFieldBinding
    private var answerButtonsEnabled = true

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentPlayFieldBinding.inflate(layoutInflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.username.observe(viewLifecycleOwner) { username ->
            username?.let {
                binding.textViewUserInfo.text = username
            }
        }

        viewModel.userPoints.observe(viewLifecycleOwner) { points ->
            points?.let {
                binding.textViewTotalPoints.text = points.toString()
            }
        }

        viewModel.selectedDifficulty.observe(viewLifecycleOwner) { selectedDifficulty ->
            selectedDifficulty?.let {
                val points = when (viewModel.selectedDifficulty.value ?: "easy") {
                    "easy" -> binding.textViewCurrentPoints.text =
                        Question.currentPointsEasy.toString()

                    "medium" -> binding.textViewCurrentPoints.text =
                        Question.currentPointsMedium.toString()

                    "hard" -> binding.textViewCurrentPoints.text =
                        Question.currentPointsHard.toString()

                    else -> binding.textViewCurrentPoints.text =
                        Question.currentPointsEasy.toString()
                }
            }
        }

        viewModel.questions.observe(viewLifecycleOwner, Observer { questions ->
            questions?.takeIf { it.isNotEmpty() }?.let {
                it[0]?.let { question -> showQuestion(question) }
                enableAnswerButtons()
            } ?: run {
                viewModel.categories.value?.let { categories ->
                    Log.d("DialogCategories", categories.toString())
                    showQuestionDialog(categories)
                }
            }
        })

        binding.logoutButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                viewModel.logout()
                findNavController().navigate(PlayFieldFragmentDirections.actionPlayFieldFragmentToWelcomeFragment())
            }
        }

        binding.exitButton.setOnClickListener {
            viewModel.viewModelScope.launch {
                viewModel.logout()
                requireActivity().finish()
            }
        }
    }

    // region Function to build Question Dialog if Category is played through

       private fun showQuestionDialog(categories: List<Category>) {
        val dialogBuilder = AlertDialog.Builder(requireContext())
        dialogBuilder.setTitle("No further questions available")
        dialogBuilder.setMessage("Please select another category:")

        val categoryNames = categories.map { it.name }

        val scrollView = ScrollView(requireContext())
        val heightInDp = 400
        val heightInPx = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP, heightInDp.toFloat(), resources.displayMetrics
        ).toInt()

        val layoutParams = ViewGroup.LayoutParams(
            ViewGroup.LayoutParams.MATCH_PARENT,
            heightInPx
        )

        val categoryListView = ListView(requireContext())
        categoryListView.adapter = ArrayAdapter(
            requireContext(),
            R.layout.list_item_layout,
            categoryNames.toTypedArray()
        )
        scrollView.addView(categoryListView, layoutParams)

        dialogBuilder.setView(scrollView)

        dialogBuilder.setPositiveButton("Abbrechen") { dialog, _ -> dialog.dismiss() }

        val dialog = dialogBuilder.create()

        val window = dialog.window
        val layoutParamsWindow = window?.attributes
        layoutParamsWindow?.width = WindowManager.LayoutParams.MATCH_PARENT
        layoutParamsWindow?.height = WindowManager.LayoutParams.WRAP_CONTENT
        window?.attributes = layoutParamsWindow

        categoryListView.setOnItemClickListener { _, _, which, _ ->
            viewModel.updateSelectedCategoryId(categories[which].id)

            viewModel.selectedDifficulty.value?.let { difficultyValue ->
                viewModel.fetchTriviaQuestionsByCategoryAndDifficulty(
                    categories[which].id,
                    difficultyValue
                ) { success ->
                    if (success) {
                        findNavController().navigate(PlayFieldFragmentDirections.actionPlayFieldFragmentSelf())
                    } else {
                        Log.e("DialogBuilder", "Error getting Categories")
                    }
                }
            }

            dialog.dismiss()
        }

        dialog.show()
    }

    // endregion

    // region Functions to handle Answer Click

    private fun onAnswerClicked(selectedAnswer: String) {
        if (!answerButtonsEnabled) {
            return
        }

        viewModel.currentQuestion.value?.let { currentQuestion ->
            answerButtonsEnabled = false

            val isCorrect =
                currentQuestion.options.find { it.text == selectedAnswer }?.correctAnswer ?: false

            viewModel.selectedDifficulty.observe(viewLifecycleOwner) { selectedDifficulty ->
                selectedDifficulty?.let {
                    val points = when (viewModel.selectedDifficulty.value ?: "easy") {
                        "easy" -> Question.currentPointsEasy
                        "medium" -> Question.currentPointsMedium
                        "hard" -> Question.currentPointsHard
                        else -> Question.currentPointsEasy
                    }

                    if (isCorrect) {
                        viewModel.updateUserPoints(points)
                        highlightAnswer(
                            selectedAnswer,
                            ContextCompat.getColor(requireContext(), R.color.green)
                        )
                    } else {
                        val correctAnswer =
                            currentQuestion.options.find { it.correctAnswer }?.text ?: ""
                        highlightAnswer(
                            selectedAnswer,
                            ContextCompat.getColor(requireContext(), R.color.red)
                        )
                        highlightCardView(
                            getCardViewForAnswer(correctAnswer),
                            ContextCompat.getColor(requireContext(), R.color.green)
                        )
                    }

                    Handler(Looper.getMainLooper()).postDelayed({
                        resetCardViewColors()
                        answerButtonsEnabled = true
                        showNextQuestion()
                    }, 2500)
                }
            }
        }
    }


    private fun getCardViewForAnswer(answer: String): MaterialCardView {
        return when (answer) {
            binding.textViewAnswerA.text.toString() -> binding.cardViewAnswerA
            binding.textViewAnswerB.text.toString() -> binding.cardViewAnswerB
            binding.textViewAnswerC.text.toString() -> binding.cardViewAnswerC
            binding.textViewAnswerD.text.toString() -> binding.cardViewAnswerD
            else -> binding.cardViewAnswerA
        }
    }

    // endregion

    // region Functions to show Questions and Options

    private fun showQuestion(question: Question) {
        binding.textViewQuestion.text = question.question

        question.options.let { options ->
            binding.textViewAnswerA.text = options.getOrNull(0)?.text
            binding.textViewAnswerB.text = options.getOrNull(1)?.text
            binding.textViewAnswerC.text = options.getOrNull(2)?.text
            binding.textViewAnswerD.text = options.getOrNull(3)?.text
        }
    }

    private fun showNextQuestion() {
        viewModel.questions.value?.let { questions ->
            val currentIndex = questions.indexOf(viewModel.currentQuestion.value)
            if (currentIndex < questions.size - 1) {
                val nextQuestion = questions[currentIndex + 1]
                viewModel.currentQuestion.postValue(nextQuestion)
                nextQuestion?.let { showQuestion(it) }
            } else {
                viewModel.selectedCategoryId.value?.let { categoryId ->
                    viewModel.selectedDifficulty.value?.let { difficultyValue ->
                        viewModel.fetchTriviaQuestionsByCategoryAndDifficulty(
                            categoryId,
                            difficultyValue,
                        ) {
                            findNavController().navigate(PlayFieldFragmentDirections.actionPlayFieldFragmentSelf())
                        }
                    }
                }
            }
        }
    }

    // endregion

    // region Functions to Highlight Answer

    private fun highlightCardView(cardView: MaterialCardView, color: Int) {
        cardView.setCardBackgroundColor(color)
    }

    private fun enableAnswerButtons() {
        binding.cardViewAnswerA.setOnClickListener { onAnswerClicked(binding.textViewAnswerA.text.toString()) }
        binding.cardViewAnswerB.setOnClickListener { onAnswerClicked(binding.textViewAnswerB.text.toString()) }
        binding.cardViewAnswerC.setOnClickListener { onAnswerClicked(binding.textViewAnswerC.text.toString()) }
        binding.cardViewAnswerD.setOnClickListener { onAnswerClicked(binding.textViewAnswerD.text.toString()) }
    }

    private fun highlightAnswer(answer: String, color: Int) {
        when (answer) {
            binding.textViewAnswerA.text.toString() -> highlightCardView(binding.cardViewAnswerA, color)
            binding.textViewAnswerB.text.toString() -> highlightCardView(binding.cardViewAnswerB, color)
            binding.textViewAnswerC.text.toString() -> highlightCardView(binding.cardViewAnswerC, color)
            binding.textViewAnswerD.text.toString() -> highlightCardView(binding.cardViewAnswerD, color)
        }
    }

    private fun resetCardViewColors() {
        binding.cardViewAnswerA.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
        binding.cardViewAnswerB.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
        binding.cardViewAnswerC.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
        binding.cardViewAnswerD.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
    }

    // endregion
}