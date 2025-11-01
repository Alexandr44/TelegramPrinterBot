package com.alexandr44.telegramprinterbot.telegram

import com.alexandr44.telegramprinterbot.dto.Constants
import org.springframework.stereotype.Service
import org.telegram.telegrambots.meta.api.objects.replykeyboard.ReplyKeyboardMarkup
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.KeyboardRow

@Service
class PrinterBotMenuBuilder {

    fun mainMenu(): ReplyKeyboardMarkup {

        val row1 = KeyboardRow()
        row1.add(Constants.ONE_PER_PAGE)

        val row2 = KeyboardRow()
        row2.add(Constants.TWO_PER_PAGE)
        row2.add(Constants.FOUR_PER_PAGE)

        val row3 = KeyboardRow()
        row3.add(Constants.MENU_SUPPORT)
        row3.add(Constants.MENU_HELP)

        return buildReplyKeyboard(row1, row2, row3)
    }

    private fun buildReplyKeyboard(vararg rows: KeyboardRow): ReplyKeyboardMarkup {
        val keyboard = ReplyKeyboardMarkup()
        keyboard.keyboard = listOf(*rows)
        keyboard.resizeKeyboard = true
        keyboard.oneTimeKeyboard = false
        return keyboard
    }

}