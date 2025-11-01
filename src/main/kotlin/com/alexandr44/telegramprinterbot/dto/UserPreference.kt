package com.alexandr44.telegramprinterbot.dto

import com.alexandr44.telegramprinterbot.enums.PageLayout

data class UserPreference(
    var pageLayout: PageLayout
)