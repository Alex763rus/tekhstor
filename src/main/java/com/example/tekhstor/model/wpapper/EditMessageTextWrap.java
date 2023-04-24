package com.example.tekhstor.model.wpapper;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.EditMessageText;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;

import static com.example.tekhstor.constant.Constant.PARSE_MODE;

@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "init", toBuilder = true)
public class EditMessageTextWrap {

    private Integer messageId;
    private String chatIdString;
    private Long chatIdLong;
    private String text;
    private InlineKeyboardMarkup inlineKeyboardMarkup;

    public EditMessageText createEditMessageText() {
        val editMessageText = new EditMessageText();
        val chatId = chatIdString == null ? String.valueOf(chatIdLong) : chatIdString;
        editMessageText.setChatId(chatId);
        editMessageText.setMessageId(messageId);
        editMessageText.setText(text);
        editMessageText.setParseMode(PARSE_MODE);
        return editMessageText;
    }
}
