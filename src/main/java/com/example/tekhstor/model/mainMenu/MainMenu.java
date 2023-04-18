package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.service.ButtonService;
import com.example.tekhstor.service.StateService;
import com.example.tekhstor.service.database.UserService;
import jakarta.persistence.MappedSuperclass;
import org.springframework.beans.factory.annotation.Autowired;

@MappedSuperclass
public abstract class MainMenu implements MainMenuActivity {

    @Autowired
    protected UserService userService;

    @Autowired
    protected StateService stateService;

    @Autowired
    protected ButtonService buttonService;

}
