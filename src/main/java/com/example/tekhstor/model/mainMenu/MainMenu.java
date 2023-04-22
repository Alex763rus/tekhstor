package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.model.jpa.ContactRepository;
import com.example.tekhstor.model.jpa.FolderRepository;
import com.example.tekhstor.model.wpapper.SendMessageWrap;
import com.example.tekhstor.service.ButtonService;
import com.example.tekhstor.service.ExcelService;
import com.example.tekhstor.service.RestService;
import com.example.tekhstor.service.StateService;
import com.example.tekhstor.service.database.UserService;
import jakarta.persistence.MappedSuperclass;
import org.springframework.beans.factory.annotation.Autowired;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.List;

@MappedSuperclass
public abstract class MainMenu implements MainMenuActivity {

    @Autowired
    protected UserService userService;

    @Autowired
    protected StateService stateService;

    @Autowired
    protected ButtonService buttonService;

    @Autowired
    protected ContactRepository contactRepository;
    @Autowired
    protected FolderRepository folderRepository;

    @Autowired
    protected ExcelService excelService;

    @Autowired
    protected RestService restService;

    private static final String DEFAULT_TEXT_ERROR = "Ошибка! Команда не найдена";

    protected List<PartialBotApiMethod> errorMessageDefault(Update update) {
        return Arrays.asList(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setText(DEFAULT_TEXT_ERROR)
                .build().createSendMessage());
    }

    protected List<PartialBotApiMethod> errorMessage(Update update, String message) {
        return Arrays.asList(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setText(message)
                .build().createSendMessage());
    }
}
