package com.example.mindguru.Repository

import UserDao
import UserEntity


class UserRepository(private val userDao: UserDao) {

    suspend fun getUserByUsername(username: String): UserEntity? {
        return userDao.getUserByUsername(username)
    }

    suspend fun insertUser(user: UserEntity) {
        userDao.insertUser(user)
    }
}