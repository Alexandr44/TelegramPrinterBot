package com.alexandr44.telegramprinterbot.config

import com.alexandr44.telegramprinterbot.telegram.PrinterBot
import com.alexandr44.telegramprinterbot.telegram.PrinterCommandHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.telegram.telegrambots.bots.TelegramLongPollingBot

@Configuration
class BotConfig {

    @Bean
    fun telegramBloggerBot(handler: PrinterCommandHandler): TelegramLongPollingBot {
        return PrinterBot(handler)
    }

}