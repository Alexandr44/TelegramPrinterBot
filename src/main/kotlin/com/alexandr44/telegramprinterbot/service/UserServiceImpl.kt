package com.alexandr44.telegramprinterbot.service

import com.alexandr44.telegramprinterbot.PageLayout
import com.alexandr44.telegramprinterbot.config.UserIdsProperty
import com.alexandr44.telegramprinterbot.dto.UserPreference
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class UserServiceImpl(
    val userIdsProperty: UserIdsProperty
) : UserService {

    val userPreferencesMap = ConcurrentHashMap<Long, UserPreference>()

    override fun checkUsername(userId: Long): Boolean {
        return userIdsProperty.userIds.contains(userId)
    }

    override fun setUserPageLayout(userId: Long, pageLayout: PageLayout) {
        userPreferencesMap[userId]?.pageLayout = pageLayout
    }

    override fun getUserPageLayout(userId: Long): PageLayout {
        return userPreferencesMap[userId]?.pageLayout ?: PageLayout.ONE
    }
}