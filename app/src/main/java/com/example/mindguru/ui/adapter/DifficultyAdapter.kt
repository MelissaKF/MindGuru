package com.example.mindguru.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.mindguru.R
import com.example.mindguru.model.Difficulty

class DifficultyAdapter(
    private var difficultyLevels: List<Difficulty>,
    private val onItemClick: (String) -> Unit
) : RecyclerView.Adapter<DifficultyAdapter.DifficultyViewHolder>() {

    inner class DifficultyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val difficultyTextView: TextView = itemView.findViewById(R.id.difficultyTV)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DifficultyViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_difficulty, parent, false)

        return DifficultyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: DifficultyViewHolder, position: Int) {
        val difficulty = difficultyLevels[position]
        holder.difficultyTextView.text = difficulty.difficulty

        holder.itemView.setOnClickListener {
            onItemClick.invoke(difficulty.difficulty)
        }
    }

    override fun getItemCount(): Int {
        return difficultyLevels.size
    }
}