package com.example.mindguru.ui

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentPlayFieldBinding
import com.example.mindguru.model.Question
import com.google.android.material.card.MaterialCardView

private const val TAG = "PlayFieldFragment"

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

        viewModel.fetchTriviaQuestions()

        viewModel.username.observe(viewLifecycleOwner, Observer { username ->
            username?.let {
                binding.textViewUserInfo.text = username
            }
        })

        viewModel.userPoints.observe(viewLifecycleOwner, Observer { points ->
            points?.let {
                binding.textViewTotalPoints.text = points.toString()
            }
        })

        viewModel.questions.observe(viewLifecycleOwner, Observer { questions ->
            questions?.let {

                questions[0]?.let { it1 -> showQuestion(it1) }

                binding.cardViewAnswerA.setOnClickListener { onAnswerClicked(binding.textViewAnswerA.text.toString()) }
                binding.cardViewAnswerB.setOnClickListener { onAnswerClicked(binding.textViewAnswerB.text.toString()) }
                binding.cardViewAnswerC.setOnClickListener { onAnswerClicked(binding.textViewAnswerC.text.toString()) }
                binding.cardViewAnswerD.setOnClickListener { onAnswerClicked(binding.textViewAnswerD.text.toString()) }
            }
        })
    }

    private fun showQuestion(question: Question) {

        binding.textViewQuestion.text = question.question

        question.options.let { options ->
            binding.textViewAnswerA.text = options.getOrNull(0)?.text
            binding.textViewAnswerB.text = options.getOrNull(1)?.text
            binding.textViewAnswerC.text = options.getOrNull(2)?.text
            binding.textViewAnswerD.text = options.getOrNull(3)?.text
        }
    }

    private fun onAnswerClicked(selectedAnswer: String) {
        if (!answerButtonsEnabled) {
            return
        }

        viewModel.currentQuestion.value?.let { currentQuestion ->
            answerButtonsEnabled = false

            val isCorrect =
                currentQuestion.options.find { it.text == selectedAnswer }?.correctAnswer ?: false

            if (isCorrect) {
                Log.d(TAG, "Richtige Antwort! Punkte erhöht.")
                viewModel.updateUserPoints(10)
                highlightAnswer(
                    selectedAnswer,
                    ContextCompat.getColor(requireContext(), R.color.green)
                )
            } else {
                Log.d(TAG, "Falsche Antwort! Punkte nicht erhöht.")
                val correctAnswer =
                    currentQuestion.options.find { it.correctAnswer }?.text ?: ""
                highlightAnswer(
                    selectedAnswer,
                    ContextCompat.getColor(requireContext(), R.color.red)
                )
                // Hervorhebung der richtigen Antwort
                highlightCardView(
                    getCardViewForAnswer(correctAnswer),
                    ContextCompat.getColor(requireContext(), R.color.green)
                )
            }

            Handler(Looper.getMainLooper()).postDelayed({
                // Zurücksetzen der Farben und Anzeigen der nächsten Frage
                resetCardViewColors()
                answerButtonsEnabled = true
                showNextQuestion()
            }, 4000)
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

    private fun showNextQuestion() {
        viewModel.questions.value?.let { questions ->
            val currentIndex = questions.indexOf(viewModel.currentQuestion.value)
            if (currentIndex < questions.size - 1) {
                val nextQuestion = questions[currentIndex + 1]
                viewModel.currentQuestion.postValue(nextQuestion)
                nextQuestion?.let { showQuestion(it) }
            } else {
                viewModel.fetchTriviaQuestions()
            }
        }
    }

    private fun highlightCardView(cardView: MaterialCardView, color: Int) {
        cardView.setCardBackgroundColor(color)
    }

    private fun highlightAnswer(answer: String, color: Int) {
        when (answer) {
            binding.textViewAnswerA.text.toString() -> highlightCardView(binding.cardViewAnswerA, color)
            binding.textViewAnswerB.text.toString() -> highlightCardView(binding.cardViewAnswerB, color)
            binding.textViewAnswerC.text.toString() -> highlightCardView(binding.cardViewAnswerC, color)
            binding.textViewAnswerD.text.toString() -> highlightCardView(binding.cardViewAnswerD, color)
        }
    }

    private fun showCorrectAnswer(correctAnswer: String) {
        when (correctAnswer) {
            binding.textViewAnswerA.text.toString() -> highlightCardView(binding.cardViewAnswerA, ContextCompat.getColor(requireContext(), R.color.green))
            binding.textViewAnswerB.text.toString() -> highlightCardView(binding.cardViewAnswerB, ContextCompat.getColor(requireContext(), R.color.green))
            binding.textViewAnswerC.text.toString() -> highlightCardView(binding.cardViewAnswerC, ContextCompat.getColor(requireContext(), R.color.green))
            binding.textViewAnswerD.text.toString() -> highlightCardView(binding.cardViewAnswerD, ContextCompat.getColor(requireContext(), R.color.green))
        }
    }

    private fun resetCardViewColors() {
        binding.cardViewAnswerA.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
        binding.cardViewAnswerB.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
        binding.cardViewAnswerC.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
        binding.cardViewAnswerD.setCardBackgroundColor(ContextCompat.getColor(requireContext(), R.color.light_blue))
    }
}