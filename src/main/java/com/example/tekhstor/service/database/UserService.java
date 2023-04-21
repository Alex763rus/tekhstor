package com.example.tekhstor.service.database;

import com.example.tekhstor.model.WhiteListUser;
import com.example.tekhstor.model.jpa.User;
import com.example.tekhstor.model.jpa.UserRepository;
import com.example.tekhstor.service.StateService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.Message;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.sql.Timestamp;

@Slf4j
@Service
public class UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private StateService stateService;

    @Autowired
    private WhiteListUser whiteListUser;

    public User getUser(Update update) {
        val message = getMessage(update);
        val chatId = message.getChatId();
        if(!whiteListUser.getWhiteListChatsID().contains(chatId)){
            return null;
        }
        User user = stateService.getUser(chatId);
        if (user == null) {
            user = userRepository.findById(chatId).orElse(registeredUser(message));
        }
        return user;
    }

    private Message getMessage(Update update) {
        if (update.hasMessage()) {
            return update.getMessage();
        }
        if (update.hasCallbackQuery()) {
            return update.getCallbackQuery().getMessage();
        }
        return null;
    }

    private User registeredUser(Message message) {
        var chatId = message.getChatId();
        var chat = message.getChat();

        User user = new User();

        user.setChatId(chatId);
        user.setFirstName(chat.getFirstName());
        user.setLastName(chat.getLastName());
        user.setUserName(chat.getUserName());
        user.setRegisteredAt(new Timestamp(System.currentTimeMillis()));

        userRepository.save(user);
        log.info("user saved: " + user);
        return user;
    }
}
