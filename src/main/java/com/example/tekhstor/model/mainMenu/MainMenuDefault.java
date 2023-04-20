package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.model.jpa.User;
import com.example.tekhstor.model.wpapper.SendMessageWrap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;

@Component
@Slf4j
public class MainMenuDefault extends MainMenu {

    final String MENU_NAME = "/default";

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public List<PartialBotApiMethod> menuRun(User user, Update update) {
        return Arrays.asList(
                SendMessageWrap.init()
                        .setChatIdLong(update.getMessage().getChatId())
                        .setText("^_^ Not a found command with name: " + update.getMessage().getText())
                        .build().createSendMessage());
    }

    @Override
    public String getDescription() {
        return MENU_NAME;
    }

}
