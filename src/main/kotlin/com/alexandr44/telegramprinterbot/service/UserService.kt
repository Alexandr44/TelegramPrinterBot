package com.alexandr44.telegramprinterbot.service

import com.alexandr44.telegramprinterbot.enums.PageLayout
import com.alexandr44.telegramprinterbot.enums.UserState

interface UserService {

    fun checkUsername(userId: Long): Boolean

    fun setUserPageLayout(userId: Long, pageLayout: PageLayout)

    fun getUserPageLayout(userId: Long): PageLayout

    fun setUserState(userId: Long, userState: UserState)

    fun getUserState(userId: Long): UserState

}