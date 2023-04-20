package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.enums.State;
import com.example.tekhstor.model.jpa.Folder;
import com.example.tekhstor.model.jpa.FolderRepository;
import com.example.tekhstor.model.jpa.User;
import com.example.tekhstor.model.wpapper.EditMessageTextWrap;
import com.example.tekhstor.model.wpapper.SendMessageWrap;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static com.example.tekhstor.enums.State.*;

@Component
@Slf4j
public class MainMenuFolder extends MainMenu {
    final String MENU_NAME = "/folder";

    @Autowired
    private FolderRepository folderRepository;

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public String getDescription() {
        return "Папки";
    }

    @Override
    public List<PartialBotApiMethod> menuRun(User user, Update update) {
        switch (stateService.getState(user)) {
            case FREE:
                return freeLogic(user, update);
            case FOLDER_MAIN:
                return folderMain(user, update);
            case FOLDER_ADD_WAIT_NAME:
                return waitFolderNameLogic(user, update);
//            case FOLDER_DELETE:
//                return folderDelete(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> freeLogic(User user, Update update) {
        val btns = new HashMap<State, String>();
        btns.put(FOLDER_SHOW, "Показать папки");
        btns.put(FOLDER_ADD, "Добавить папку");
        btns.put(FOLDER_DELETE, "Удалить папку");
        stateService.setState(user, FOLDER_MAIN);

        return Arrays.asList(
                SendMessageWrap.init()
                        .setChatIdLong(update.getMessage().getChatId())
                        .setText("Выберите режим работы с папками:")
                        .setInlineKeyboardMarkup(buttonService.createVerticalMenuState(btns))
                        .build().createSendMessage());
    }

    private List<PartialBotApiMethod> folderMain(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        val callBackData = update.getCallbackQuery().getData();
        if (callBackData.equals(String.valueOf(FOLDER_SHOW))) {
            return folderShow(user, update);
        }
        if (callBackData.equals(String.valueOf(FOLDER_ADD))) {
            return folderAdd(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> folderShow(User user, Update update) {
        val folderList = (List<Folder>) folderRepository.findAll();
        val messageText = new StringBuilder("Доступные папки");
        messageText.append(folderList.size() == 0 ? " отсутствуют\n" : ":\n");
        for (int i = 0; i < folderList.size(); ++i) {
            messageText.append(i + 1).append(") ").append(folderList.get(i).getName()).append("\r\n");
        }
        stateService.setState(user, State.FREE);
        return Arrays.asList(
                EditMessageTextWrap.init()
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                        .setText(messageText.toString())
                        .build().createEditMessageText());
    }

    private final String FOLDER_ADD_TEXT = "Режим добавления новой папки.\nВведите название новой папки:";
    private final String FOLDER_ADD_OK_SAVE = "Новая папка успешно сохранена";

    private List<PartialBotApiMethod> folderAdd(User user, Update update) {
        stateService.setState(user, FOLDER_ADD_WAIT_NAME);
        return Arrays.asList(
                EditMessageTextWrap.init()
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                        .setText(FOLDER_ADD_TEXT)
                        .build().createEditMessageText());
    }

    private List<PartialBotApiMethod> waitFolderNameLogic(User user, Update update) {
        val folder = new Folder();
        folder.setName(update.getMessage().getText());
        folderRepository.save(folder);
        stateService.setState(user, State.FREE);
        return Arrays.asList(
                SendMessageWrap.init()
                        .setChatIdLong(update.getMessage().getChatId())
                        .setText(FOLDER_ADD_OK_SAVE)
                        .build().createSendMessage());
    }
}
