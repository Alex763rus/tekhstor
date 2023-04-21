package com.example.tekhstor.service;

import com.example.tekhstor.api.SearchPublicChats;
import com.example.tekhstor.api.SendTgMessage;
import com.example.tekhstor.api.Text;
import com.example.tekhstor.config.BotConfig;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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

    @Autowired
    SearchPublicChats searchPublicChats;

    @PostConstruct
    public void init() {
        sendTgMessage.setApi_key(botConfig.getSimApiKey());
        searchPublicChats.setApi_key(botConfig.getSimApiKey());
    }

    public String getChatInfo(String userName) {
        searchPublicChats.setUsername(userName);
        return sendPostRequest(searchPublicChats);
    }

    public String sendMessage(String chatId, String message) {
        sendTgMessage.setChat_id(chatId);
        text.setText(message);
        return sendPostRequest(sendTgMessage);
    }
    private String sendPostRequest(Object obj) {
        try {
            val restTemplate = new RestTemplate();
            val json = (new ObjectMapper().writeValueAsString(obj)).replace("\"type\"", "\"@type\"");
            val headers = new HttpHeaders();
            headers.setContentType(MediaType.APPLICATION_JSON);
            val httpEntity = new HttpEntity<>(json, headers);

            val responseEntity = restTemplate.exchange(URL, HttpMethod.POST, httpEntity, String.class);
            return responseEntity.getBody();
        } catch (Exception e) {
            log.error(e.getMessage());
        }
        return null;
    }
}
