package com.example.mindguru.data.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.mindguru.data.UserEntity

@Dao
interface UserDao {
    @Insert
    suspend fun insertUser(user: UserEntity)

    @Query("SELECT * FROM user WHERE username = :username")
    suspend fun getUserByUsername(username: String): UserEntity?
}