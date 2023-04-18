package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.model.jpa.User;
import com.vdurmont.emoji.EmojiParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class MainMenuStart extends MainMenu {

    final String MENU_NAME = "/start";

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public SendMessage menuRun(User user, Update update) {
        String answer = EmojiParser.parseToUnicode("Hello, " + user.getFirstName() + "!" + " :blush:");
        return new SendMessage(user.getChatId().toString(),  answer);
    }

    @Override
    public String getDescription() {
        return " Поехали!";
    }
}
