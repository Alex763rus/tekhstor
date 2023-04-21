package com.example.tekhstor.api;

import jakarta.annotation.PostConstruct;
import lombok.Data;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
@Data
public class InputMessageContent {

    @PostConstruct
    public void init() {
        setType("inputMessageText");
        setDisable_web_page_preview(false);
    }

    private String type;
    private Boolean disable_web_page_preview;

    @Autowired
    private Text text;

}