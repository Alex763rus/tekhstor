package com.example.tekhstor.api;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
public class SendTgMessage {
    @PostConstruct
    public void init() {
        setType("sendMessage");
        setDisable_notification(true);
    }

    private String api_key;
    private String type;
    private String chat_id;

    private Boolean disable_notification;

    @Autowired
    private InputMessageContent input_message_content;

}
