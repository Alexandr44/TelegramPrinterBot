package com.alexandr44.telegramprinterbot.telegram

import com.alexandr44.telegramprinterbot.service.UserService
import org.springframework.beans.factory.annotation.Value
import org.telegram.telegrambots.bots.TelegramLongPollingBot
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.api.objects.Update


class PrinterBot(
    private val handler: PrinterCommandHandler,
    private val userService: UserService
) : TelegramLongPollingBot("8438331015:AAF6eWXQU3htN4Kr4ybYjCjUcgzpS-b0BY4") {

    @Value("\${telegrambot.bot.username}")
    private lateinit var botUsername: String

    override fun getBotUsername() = botUsername

    override fun onUpdateReceived(update: Update?) {
        if (update == null || !update.hasMessage()) return

        val msg: Message = update.message

        if (!userService.checkUsername(msg.from.id)) {
            execute(
                SendMessage(
                    msg.chatId.toString(),
                    "Добро пожаловать! К сожалению вы не в списке доступных пользователей"
                )
            )
            return
        }

        if (msg.hasDocument()) {
            handler.handleDocument(msg, { getFile: GetFile -> execute(getFile) }, { sendMessage: BotApiMethodMessage ->
                execute(sendMessage)
            })
        } else if (msg.hasPhoto()) {
            handler.handlePhoto(msg, { getFile: GetFile -> execute(getFile) }, { sendMessage: BotApiMethodMessage ->
                execute(sendMessage)
            })
        } else if (msg.hasText()) {
            handler.handleTextMessage(msg) { sendMessage: BotApiMethodMessage ->
                execute(sendMessage)
            }
        }
    }
}