package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.model.jpa.*;
import com.example.tekhstor.model.wpapper.EditMessageTextWrap;
import com.example.tekhstor.model.wpapper.SendMessageWrap;
import com.example.tekhstor.service.RestService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.tekhstor.constant.Constant.NEW_LINE;
import static com.example.tekhstor.enums.State.*;

@Component
@Slf4j
public class MainMenuFolderMessage extends MainMenu {
    final String MENU_NAME = "/folder_message";

    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private ContactRepository contactRepository;

    @Autowired
    private RestService restService;

    private Map<User, Folder> folderTmp = new HashMap();

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public String getDescription() {
        return "Отправить сообщение группе";
    }

    @Override
    public List<PartialBotApiMethod> menuRun(User user, Update update) {
        switch (stateService.getState(user)) {
            case FREE:
                return freeLogic(user, update);
            case FOLDER_MESSAGE_WAIT_FOLDER:
                return waitFolderLogic(user, update);
            case FOLDER_MESSAGE_WAIT_MESSAGE:
                return sendMessages(user, update);
        }
        return errorMessageDefault(update);

    }

    private List<PartialBotApiMethod> sendMessages(User user, Update update) {
        val contacts = contactRepository.getContatsByFolderAndIsDelete(folderTmp.get(user), false);
        val message = update.getMessage().getText();
        for (Contact contact : contacts) {
            restService.sendMessage(userService.getApiKey(user), contact.getChatId(), message);
        }
        stateService.setState(user, FREE);
        return Arrays.asList(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setText("Сообщения успешно отправлены:" + contacts.size())
                .build().createSendMessage());
    }

    private List<PartialBotApiMethod> waitFolderLogic(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        stateService.setState(user, FOLDER_MESSAGE_WAIT_MESSAGE);
        val folder = folderRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get();
        folderTmp.put(user, folder);
        return Arrays.asList(EditMessageTextWrap.init()
                .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setText("Выбрана папка:" + folder.getName() + NEW_LINE + "Введите сообщение:")
                .build().createEditMessageText());
    }

    private List<PartialBotApiMethod> freeLogic(User user, Update update) {
        val folderList = (List<Folder>) folderRepository.getFoldersByIsDelete(false);
        val btns = new HashMap<String, String>();
        if (folderList.size() == 0) {
            return errorMessage(update, "Папки с контактами отсутствуют. Отправка невозможна");
        }
        for (int i = 0; i < folderList.size(); ++i) {
            btns.put(String.valueOf(folderList.get(i).getFolderId()), folderList.get(i).getName());
        }
        stateService.setState(user, FOLDER_MESSAGE_WAIT_FOLDER);
        return Arrays.asList(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setInlineKeyboardMarkup(buttonService.createVerticalMenu(btns))
                .setText("Выберите папку для рассылки:\n")
                .build().createSendMessage());
    }
}
