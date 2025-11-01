package com.alexandr44.telegramprinterbot.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.stereotype.Component

@Component
@ConfigurationProperties(prefix = "telegrambot.users")
data class UserIdsProperty(
    var userIds: List<Long> = emptyList()
)