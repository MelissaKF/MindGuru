package com.example.mindguru.ui

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Observer
import com.example.mindguru.R
import com.example.mindguru.databinding.FragmentPlayFieldBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

private const val TAG = "PlayFieldFragment"

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

        Log.d(TAG, "onViewCreated")

        viewModel.fetchQuestionFromFirestore()

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

        viewModel.currentPoints.observe(viewLifecycleOwner, Observer { points ->
            Log.d("Fragment", "Observer called. Points: $points")
            points?.let {
                binding.textViewCurrentPoints.text = points.toString()
            }
        })

        // Beobachte die LiveData für die Frage und aktualisiere die Benutzeroberfläche
        viewModel.currentQuestion.observe(viewLifecycleOwner, Observer { question ->
            Log.d(TAG, "Current question observed: $question")

            // Hier kannst du die Frage in deiner Benutzeroberfläche aktualisieren
            binding.textViewQuestion.text = question?.questionText

            // Überprüfe, ob die Frage und die Optionen nicht null sind
            question?.options?.let { options ->
                binding.textViewAnswerA.text = options["OptionA"]?.text
                binding.textViewAnswerB.text = options["OptionB"]?.text
                binding.textViewAnswerC.text = options["OptionC"]?.text
                binding.textViewAnswerD.text = options["OptionD"]?.text
            }
        })

        fun updateUIForAnswer(selectedOptionId: String, isCorrect: Boolean) {
            // Hier kannst du die Hintergrundfarbe basierend auf der Richtigkeit der Antwort ändern
            val colorResId = if (isCorrect) R.color.green else R.color.red

            Log.d("Answer", "Selected Option: $selectedOptionId, Is Correct: $isCorrect")

            when (selectedOptionId) {
                "OptionA" -> binding.cardViewAnswerA.setBackgroundResource(colorResId)
                "OptionB" -> binding.cardViewAnswerB.setBackgroundResource(colorResId)
                "OptionC" -> binding.cardViewAnswerC.setBackgroundResource(colorResId)
                "OptionD" -> binding.cardViewAnswerD.setBackgroundResource(colorResId)
            }
        }

        fun handleAnswerClick(selectedOptionId: String) {
            val isCorrect = viewModel.checkAnswer(selectedOptionId)
            updateUIForAnswer(selectedOptionId, isCorrect)

                binding.textViewTotalPoints.text = viewModel.getUserPoints().toString()
                binding.textViewCurrentPoints.text = viewModel.getCurrentPoints().toString()
            }


        // Beispiel: Setze einen OnClickListener für eine Option
        binding.cardViewAnswerA.setOnClickListener {
            handleAnswerClick( "OptionA" )
        }

        binding.cardViewAnswerB.setOnClickListener {
            handleAnswerClick("OptionB")
        }

        binding.cardViewAnswerC.setOnClickListener {
            handleAnswerClick("OptionC")
        }

        binding.cardViewAnswerD.setOnClickListener {
            handleAnswerClick("OptionD")
        }
    }
}