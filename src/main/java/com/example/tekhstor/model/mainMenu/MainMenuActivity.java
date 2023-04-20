package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.model.jpa.User;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.List;


public interface MainMenuActivity {

    public String getMenuName();

    public String getDescription();

    public List<PartialBotApiMethod> menuRun(User user, Update update);

}
