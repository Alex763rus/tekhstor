package com.example.tekhstor.service;

import com.example.tekhstor.model.jpa.User;
import com.example.tekhstor.model.mainMenu.MainMenuActivity;
import com.example.tekhstor.model.mainMenu.MainMenuDefault;
import com.example.tekhstor.model.mainMenu.MainMenuStart;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.commands.BotCommand;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class MainMenuService {

    @Autowired
    private MainMenuStart mainMenuStart;
    @Autowired
    private MainMenuDefault mainMenuActivityDefault;

    @Autowired
    private StateService stateService;
    private List<MainMenuActivity> mainMenu;

    @PostConstruct
    public void mainMenuInit() {
        mainMenu = new ArrayList();
        mainMenu.add(mainMenuStart);
    }

    public PartialBotApiMethod messageProcess(User user, Update update) {
        MainMenuActivity mainMenuActivity = mainMenu.stream()
                .filter(e -> update.hasMessage() && e.getMenuName().equals(update.getMessage().getText()))
                .findFirst().get();

        if (mainMenuActivity != null) {
            stateService.setMenu(user, mainMenuActivity);
        } else {
            mainMenuActivity = stateService.getMenu(user);
            if (mainMenuActivity == null) {
                log.warn("Не найдена команда с именем: " + update.getMessage().getText());
                mainMenuActivity = mainMenuActivityDefault;
            }
        }
        PartialBotApiMethod mainMenuAnswer = mainMenuActivity.menuRun(user, update);
        return mainMenuAnswer;
    }

    public List<BotCommand> getMainMenuComands() {
        List<BotCommand> listofCommands = new ArrayList<>();
        for (MainMenuActivity mainMenuActivity : mainMenu) {
            listofCommands.add(new BotCommand(mainMenuActivity.getMenuName(), mainMenuActivity.getDescription()));
        }
        return listofCommands;
    }
}
