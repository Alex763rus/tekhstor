package com.example.tekhstor.service;

import com.example.tekhstor.model.jpa.User;
import com.example.tekhstor.model.mainMenu.*;
import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
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
    private MainMenuFolder mainMenuFolder;

    @Autowired
    private MainMenuContact mainMenuContact;
    @Autowired
    private MainMenuFolderMessage mainMenuFolderMessage;
    @Autowired
    private StateService stateService;
    private List<MainMenuActivity> mainMenu;

    @PostConstruct
    public void mainMenuInit() {
        mainMenu = new ArrayList();
        mainMenu.add(mainMenuStart);
        mainMenu.add(mainMenuFolder);
        mainMenu.add(mainMenuContact);
        mainMenu.add(mainMenuFolderMessage);
    }

    public List<PartialBotApiMethod> messageProcess(User user, Update update) {
        MainMenuActivity mainMenuActivity = null;
        if(update.hasMessage()) {
            for (val menu : mainMenu) {
                if (menu.getMenuName().equals(update.getMessage().getText())){
                    mainMenuActivity = menu;
                }
            }
        }
        if (mainMenuActivity != null) {
            stateService.setMenu(user, mainMenuActivity);
        } else {
            mainMenuActivity = stateService.getMenu(user);
            if (mainMenuActivity == null) {
                log.warn("Не найдена команда с именем: " + update.getMessage().getText());
                mainMenuActivity = mainMenuActivityDefault;
            }
        }
        return mainMenuActivity.menuRun(user, update);
    }

    public List<BotCommand> getMainMenuComands() {
        List<BotCommand> listofCommands = new ArrayList<>();
        for (MainMenuActivity mainMenuActivity : mainMenu) {
            listofCommands.add(new BotCommand(mainMenuActivity.getMenuName(), mainMenuActivity.getDescription()));
        }
        return listofCommands;
    }
}
