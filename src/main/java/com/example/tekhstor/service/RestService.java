package com.example.tekhstor.service;

import com.example.tekhstor.api.InputMessageContent;
import com.example.tekhstor.api.SendTgMessage;
import com.example.tekhstor.api.Text;
import com.example.tekhstor.config.BotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.*;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
@Slf4j
public class RestService {

    private final String URL = "https://api.tdlib.org/client";
    @Autowired
    BotConfig botConfig;

    @Autowired
    SendTgMessage sendTgMessage;

    @Autowired
    Text text;
    @PostConstruct
    public void init() {
        sendTgMessage.setApi_key(botConfig.getSimApiKey());
    }

    public String sendPost(String chatId, String message) {
        sendTgMessage.setChat_id(chatId);
        text.setText(message);

        RestTemplate restTemplate = new RestTemplate();
        try {
            String json = (new ObjectMapper().writeValueAsString(sendTgMessage)).replace("\"type\"", "\"@type\"");
            HttpHeaders headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            HttpEntity<String> httpEntity = new HttpEntity<>(json, headers);
            ResponseEntity<String> responseEntity = restTemplate
                    .exchange(URL, HttpMethod.POST, httpEntity, String.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
