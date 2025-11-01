package com.alexandr44.telegramprinterbot

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication

@SpringBootApplication
class TelegramPrinterBotApplication {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            runApplication<TelegramPrinterBotApplication>(*args)
        }
    }
}