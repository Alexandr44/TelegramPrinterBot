package com.alexandr44.telegramprinterbot.telegram

import com.alexandr44.telegramprinterbot.PageLayout
import com.alexandr44.telegramprinterbot.dto.Constants
import com.alexandr44.telegramprinterbot.service.PrintService
import com.alexandr44.telegramprinterbot.service.UserService
import mu.KotlinLogging
import org.springframework.beans.factory.annotation.Value
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.methods.GetFile
import org.telegram.telegrambots.meta.api.methods.botapimethods.BotApiMethodMessage
import org.telegram.telegrambots.meta.api.methods.send.SendMessage
import org.telegram.telegrambots.meta.api.objects.File
import org.telegram.telegrambots.meta.api.objects.Message
import org.telegram.telegrambots.meta.exceptions.TelegramApiException


@Service
class PrinterCommandHandler(
    val menuBuilder: PrinterBotMenuBuilder,
    val printService: PrintService,
    val userService: UserService,
) {

    private val log = KotlinLogging.logger {}

    @Value("\${telegrambot.bot.support_chat_id}")
    private lateinit var supportChatId: String

    @Value("\${telegrambot.bot.token}")
    private lateinit var botToken: String

    fun handleTextMessage(message: Message, execute: (BotApiMethodMessage) -> Message) {
        val chatId: Long = message.chatId
        val text: String = message.text
        val userId: Long = message.from.id

        if (!userService.checkUsername(userId)) {
            execute(
                SendMessage(
                    chatId.toString(),
                    "Добро пожаловать! К сожалению вы не в списке доступных пользователей"
                )
            )
            return
        }

        if (text == "/start") {
            val msg = SendMessage(chatId.toString(), "Добро пожаловать! Пришлите мне файлик и я его распечатаю")
            msg.replyMarkup = menuBuilder.mainMenu()
            execute(msg)
            return
        } else if (text.startsWith("/reply")) {
            handleReplyMessage(text, execute)
        } else {
            if (!handleMenuButtons(text, chatId, userId, execute)) {
//                val availableRequests = bloggerSubscriptionService.getAvailableRequests(userId)
//                    ?: bloggerSubscriptionService.addFreeUser(userId).requestsLeft
//
//                if (availableRequests > 0) {
//                    val msg = SendMessage(
//                        chatId.toString(),
//                        handleStateMessage(
//                            message
//                        ) { sendMessage: SendMessage -> execute(sendMessage) }
//                    )
//                    execute(msg)
//                    bloggerSubscriptionService.decreaseRequest(userId)
//                } else {
//                    val msg = SendMessage(
//                        userId.toString(),
//                        "Закончились доступные запросы на генерацию! Нужно оплатить новые"
//                    )
//                    execute(msg)
//                }
            }
        }
    }

    fun handleDocument(msg: Message, fileExecutor: (GetFile) -> File, messageSender: (BotApiMethodMessage) -> Message) {
        val doc = msg.document
        val fileId = doc.fileId
        val fileName = doc.fileName
        downloadAndPrint(msg, fileId, fileName, fileExecutor, messageSender)
    }

    fun handlePhoto(msg: Message, fileExecutor: (GetFile) -> File, messageSender: (BotApiMethodMessage) -> Message) {
        val photo = msg.photo[msg.photo.size - 1]
        val fileId = photo.fileId
        val fileName = "photo_" + System.currentTimeMillis() + ".jpg"
        downloadAndPrint(msg, fileId, fileName, fileExecutor, messageSender)
    }

    private fun downloadAndPrint(
        msg: Message,
        fileId: String,
        fileName: String,
        fileExecutor: (GetFile) -> File,
        messageSender: (BotApiMethodMessage) -> Message
    ) {
        try {
            // Скачиваем файл
            val getFile = GetFile(fileId)
            val file: File = fileExecutor.invoke(getFile)
            val fileUrl = file.getFileUrl(botToken)

            printService.printDocument(fileUrl, fileName, PageLayout.ONE)

            messageSender.invoke(buildMessage(msg.chatId, "✅ Отправлено на печать"))
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            messageSender.invoke(buildMessage(msg.chatId, "❌ Ошибка при печати: " + e.message))
        }
    }

    private fun buildMessage(chatId: Long, text: String): SendMessage {
        return SendMessage.builder()
            .chatId(chatId.toString())
            .text(text)
            .build()
    }

    private fun handleReplyMessage(text: String, execute: (BotApiMethodMessage) -> Message) {
        val parts: List<String> = text.split(" ")
        if (parts.size < 3) {
            execute(SendMessage(supportChatId, "❌ Использование: /reply <chatId> <текст>"))
            return
        }

        val chatId: Long = parts[1].toLong()
        val replyText = parts.drop(2).joinToString(" ")

        val msg = SendMessage(chatId.toString(), "💬 Ответ поддержки:\n$replyText")
        try {
            execute(msg)
            execute(SendMessage(supportChatId, "✅ Ответ отправлен пользователю $chatId"))
        } catch (e: TelegramApiException) {
            execute(SendMessage(supportChatId, "❌ Не удалось отправить сообщение пользователю."))
            e.printStackTrace()
        }
    }

    private fun handleMenuButtons(
        text: String,
        chatId: Long,
        userId: Long,
        execute: (BotApiMethodMessage) -> Message
    ): Boolean {
        when (text) {

            Constants.ONE_PER_PAGE -> {
                val msg = SendMessage(chatId.toString(), "Выбрана печать 1 страница на 1")
                execute(msg)
            }

            Constants.TWO_PER_PAGE -> {
                val msg = SendMessage(chatId.toString(), "Выбрана печать 2 страницы на 1")
                execute(msg)
            }

            Constants.FOUR_PER_PAGE -> {
                val msg = SendMessage(chatId.toString(), "Выбрана печать 4 страницы на 1")
                execute(msg)
            }

            Constants.MENU_SUPPORT -> {
                val str = """
                        |🧑‍💻 Напишите сообщение в поддержку.
                        |Мы ответим вам как можно скорее.
                        """.trimMargin()
                val msg = SendMessage(chatId.toString(), str)
//                bloggerStateService.addState(userId, BloggerUserState.AWAITING_SUPPORT_MESSAGE)
                execute(msg)
            }

            Constants.MENU_HELP -> {
                val str = """
                        |🧑‍💻 Пришли мне файлик и я его распечатаю.
                        |В меню можно выбрать, сколько страниц печатать на одной странице.
                        """.trimMargin()
                val msg = SendMessage(chatId.toString(), str)
                execute(msg)
            }

            else -> {
                return false
            }
        }
        return true
    }


}