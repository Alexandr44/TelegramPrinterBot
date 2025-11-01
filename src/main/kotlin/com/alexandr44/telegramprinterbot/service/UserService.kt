package com.alexandr44.telegramprinterbot.service

interface UserService {

    fun checkUsername(userId: Long): Boolean

}