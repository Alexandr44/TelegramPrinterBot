package com.alexandr44.telegramprinterbot.service

import com.alexandr44.telegramprinterbot.PageLayout

interface UserService {

    fun checkUsername(userId: Long): Boolean

    fun setUserPageLayout(userId: Long, pageLayout: PageLayout)

    fun getUserPageLayout(userId: Long): PageLayout

}