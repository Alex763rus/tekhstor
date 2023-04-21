package com.example.tekhstor.config;

import com.example.tekhstor.model.WhiteListUser;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.Data;
import lombok.SneakyThrows;
import lombok.val;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import java.io.File;

@Configuration
@Data
@PropertySource("application.properties")
public class BotConfig {

    @Value("${bot.version}")
    String botVersion;

    @Value("${bot.username}")
    String botUserName;

    @Value("${bot.token}")
    String botToken;

    @SneakyThrows
    @Bean
    WhiteListUser whiteListUser(){
        val currentDir = System.getProperty("user.dir");
        val filePath = currentDir + "\\" + "WhiteListUsers.json";
        val objectMapper = new ObjectMapper();
        val result = objectMapper.readValue(new File(filePath), WhiteListUser.class);
        return result;
    }
}
