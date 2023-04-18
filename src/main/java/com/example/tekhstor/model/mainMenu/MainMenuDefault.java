package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.model.jpa.User;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

@Component
@Slf4j
public class MainMenuDefault extends MainMenu {

    final String MENU_NAME = "/default";

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public PartialBotApiMethod menuRun(User user, Update update) {
        String chatId = String.valueOf(update.getMessage().getChatId());
        String command = update.getMessage().getText();
        return new SendMessage(chatId, "^_^ Not a found command with name: " + command);
    }

    @Override
    public String getDescription() {
        return MENU_NAME;
    }

}
