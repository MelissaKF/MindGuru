package com.example.mindguru.Repository

import com.example.mindguru.data.UserEntity
import com.example.mindguru.data.database.UserDao


class UserRepository(private val userDao: UserDao) {

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }
}