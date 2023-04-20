package com.example.tekhstor.model.wpapper;

import lombok.Getter;
import lombok.experimental.SuperBuilder;
import lombok.val;
import org.telegram.telegrambots.meta.api.methods.updatingmessages.DeleteMessage;

@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "init", toBuilder = true)
public class DeleteMessageWrap {

    private Integer messageId;
    private String chatIdString;
    private Long chatIdLong;

    public DeleteMessage createDeleteMessage() {
        val deleteMessageText = new DeleteMessage();
        val chatId = chatIdString == null ? String.valueOf(chatIdLong) : chatIdString;
        deleteMessageText.setChatId(chatId);
        deleteMessageText.setMessageId(messageId);
        return deleteMessageText;
    }
}
