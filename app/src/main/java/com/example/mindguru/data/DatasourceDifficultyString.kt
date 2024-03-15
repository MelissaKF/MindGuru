package com.example.mindguru.data

import com.example.mindguru.model.Difficulty

class DatasourceDifficultyString {
    fun loadData() : List<Difficulty>{
        return listOf(
            Difficulty("easy"),
            Difficulty("medium"),
            Difficulty("hard")
        )
    }
}