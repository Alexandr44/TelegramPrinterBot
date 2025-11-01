package com.alexandr44.telegramprinterbot.telegram

import com.alexandr44.telegramprinterbot.dto.Constants
import com.alexandr44.telegramprinterbot.enums.PageLayout
import com.alexandr44.telegramprinterbot.enums.UserState
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

        if (text == "/start") {
            val msg = SendMessage(chatId.toString(), "Добро пожаловать! Пришлите мне файлик и я его распечатаю")
            msg.replyMarkup = menuBuilder.mainMenu()
            execute(msg)
            return
        } else if (text.startsWith("/reply")) {
            handleReplyMessage(text, execute)
        } else {
            if (!handleMenuButtons(text, chatId, userId, execute)) {
                when(userService.getUserState(userId)) {
                    UserState.OK -> execute(SendMessage(chatId.toString(), "Ничего не знаю, файл давай"))
                    UserState.SUPPORT_MESSAGE -> {
                        val supportMsg = """
                            🆘 Новое сообщение от пользователя:
                            👤 ID: %d
                            🔗 @%s
                            💬 %s
                            """.trimIndent().format(userId, message.from.userName ?: "без username", text)

                        val toSupport = SendMessage()
                        toSupport.chatId = supportChatId
                        toSupport.text = supportMsg
                        execute.invoke(toSupport)
                        userService.setUserState(userId, UserState.OK)
                        execute(SendMessage(chatId.toString(), "✅ Сообщение отправлено в поддержку. Спасибо!"))
                    }
                }
            }
        }
    }

    fun handleDocument(msg: Message, fileExecutor: (GetFile) -> File, messageSender: (BotApiMethodMessage) -> Message) {
        val doc = msg.document
        val fileId = doc.fileId
        val fileName = doc.fileName
        val resultMessage = downloadAndPrint(fileId, fileName, userService.getUserPageLayout(msg.from.id), fileExecutor)
        messageSender.invoke(buildMessage(msg.chatId, resultMessage))
    }

    fun handlePhoto(msg: Message, fileExecutor: (GetFile) -> File, messageSender: (BotApiMethodMessage) -> Message) {
        val photo = msg.photo[msg.photo.size - 1]
        val fileId = photo.fileId
        val fileName = "photo_" + System.currentTimeMillis() + ".jpg"
        val resultMessage = downloadAndPrint(fileId, fileName, userService.getUserPageLayout(msg.from.id), fileExecutor)
        messageSender.invoke(buildMessage(msg.chatId, resultMessage))
    }

    private fun downloadAndPrint(
        fileId: String,
        fileName: String,
        pageLayout: PageLayout,
        fileExecutor: (GetFile) -> File,
    ): String {
        try {
            // Скачиваем файл
            val getFile = GetFile(fileId)
            val file: File = fileExecutor.invoke(getFile)
            val fileUrl = file.getFileUrl(botToken)

            printService.printDocument(fileUrl, fileName, pageLayout)

            return "✅ Отправлено на печать"
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
            return "❌ Ошибка при печати: " + e.message
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
                userService.setUserPageLayout(userId, PageLayout.ONE)
                val msg = SendMessage(chatId.toString(), "Выбрана печать 1 страница на 1")
                execute(msg)
            }

            Constants.TWO_PER_PAGE -> {
                userService.setUserPageLayout(userId, PageLayout.TWO)
                val msg = SendMessage(chatId.toString(), "Выбрана печать 2 страницы на 1")
                execute(msg)
            }

            Constants.FOUR_PER_PAGE -> {
                userService.setUserPageLayout(userId, PageLayout.FOUR)
                val msg = SendMessage(chatId.toString(), "Выбрана печать 4 страницы на 1")
                execute(msg)
            }

            Constants.MENU_SUPPORT -> {
                val str = """
                        |🧑‍💻 Напишите сообщение в поддержку.
                        |Мы ответим вам как можно скорее.
                        """.trimMargin()
                val msg = SendMessage(chatId.toString(), str)
                userService.setUserState(userId, UserState.SUPPORT_MESSAGE)
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