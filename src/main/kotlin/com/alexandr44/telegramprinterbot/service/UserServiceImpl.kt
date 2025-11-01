package com.alexandr44.telegramprinterbot.service

import com.alexandr44.telegramprinterbot.config.UserIdsProperty
import org.springframework.stereotype.Service

@Service
class UserServiceImpl(
    val userIdsProperty: UserIdsProperty
) : UserService {

    override fun checkUsername(userId: Long): Boolean {
        return userIdsProperty.userIds.contains(userId)
    }

}