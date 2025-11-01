package com.alexandr44.telegramprinterbot.service

import com.alexandr44.telegramprinterbot.PageLayout

interface PrintService {

    fun printDocument(fileUrl: String, fileName: String, pageLayout: PageLayout)

}