package com.example.mindguru.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user")
data class UserEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val username: String,
    val password : String,
    val points: Int = 0,
    val difficultyLevel: Int = 1,
)