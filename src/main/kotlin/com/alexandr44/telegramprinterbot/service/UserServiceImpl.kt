package com.alexandr44.telegramprinterbot.service

import com.alexandr44.telegramprinterbot.config.UserIdsProperty
import com.alexandr44.telegramprinterbot.dto.UserPreference
import com.alexandr44.telegramprinterbot.enums.PageLayout
import com.alexandr44.telegramprinterbot.enums.UserState
import org.springframework.stereotype.Service
import java.util.concurrent.ConcurrentHashMap

@Service
class UserServiceImpl(
    val userIdsProperty: UserIdsProperty
) : UserService {

    val userPreferencesMap = ConcurrentHashMap<Long, UserPreference>()
    val userStateMap = ConcurrentHashMap<Long, UserState>()

    override fun checkUsername(userId: Long): Boolean {
        return userIdsProperty.userIds.contains(userId)
    }

    override fun setUserPageLayout(userId: Long, pageLayout: PageLayout) {
        userPreferencesMap[userId]?.pageLayout = pageLayout
    }

    override fun getUserPageLayout(userId: Long): PageLayout {
        return userPreferencesMap[userId]?.pageLayout ?: PageLayout.ONE
    }

    override fun setUserState(userId: Long, userState: UserState) {
        userStateMap[userId] = userState
    }

    override fun getUserState(userId: Long): UserState {
        if (!userStateMap.containsKey(userId)) {
            userStateMap[userId] = UserState.OK
        }
        return userStateMap[userId]!!
    }
}