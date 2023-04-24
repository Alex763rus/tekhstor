package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.enums.State;
import com.example.tekhstor.model.jpa.*;
import com.example.tekhstor.model.wpapper.DeleteMessageWrap;
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
import java.util.LinkedHashMap;
import java.util.List;

import static com.example.tekhstor.constant.StringConstant.NEW_LINE;
import static com.example.tekhstor.constant.StringConstant.SPACE;
import static com.example.tekhstor.enums.State.*;

@Component
@Slf4j
public class MainMenuFolder extends MainMenu {
    final String MENU_NAME = "/folder";

    private final String FOLDER_ADD_TEXT = "Режим добавления новой папки" + NEW_LINE + "Введите название новой папки:";
    private final String FOLDER_DELETE_TEXT = "Режим удаления папки." + NEW_LINE + "Укажите папку:";
    private final String FOLDER_ADD_OK_SAVE = "Новая папка успешно сохранена";

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
            case FOLDER_DELETE_WAIT_NAME:
                return folderDeleteWaitNameLogic(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> freeLogic(User user, Update update) {
        val btns = new LinkedHashMap<State, String>();
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
        if (callBackData.equals(String.valueOf(FOLDER_DELETE))) {
            return folderDelete(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> folderDeleteWaitNameLogic(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        val folder = folderRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get();
        List<Contact> contacts = contactRepository.getContatsByFolderAndIsDelete(folder, false);
        for (Contact contact : contacts) {
            contact.setIsDelete(true);
            contactRepository.save(contact);
        }
        folder.setIsDelete(true);
        folderRepository.save(folder);
        stateService.setState(user, FREE);
        return Arrays.asList(EditMessageTextWrap.init()
                .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setText("Папка с контактами успешно удалена: " + folder.getName())
                .build().createEditMessageText());
    }

    private List<PartialBotApiMethod> folderDelete(User user, Update update) {
        val btns = new LinkedHashMap<String, String>();
        val folderList = (List<Folder>) folderRepository.getFoldersByIsDelete(false);
        val messageText = FOLDER_DELETE_TEXT + (folderList.size() == 0 ? "Папки отсутствуют" : "") + NEW_LINE;
        for (int i = 0; i < folderList.size(); ++i) {
            btns.put(String.valueOf(folderList.get(i).getFolderId()), folderList.get(i).getName());
        }
        val chatId = update.getCallbackQuery().getMessage().getChatId();
        val deleteMessage = DeleteMessageWrap.init()
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setChatIdLong(chatId)
                .build().createDeleteMessage();
        val chooseContact = SendMessageWrap.init()
                .setChatIdLong(chatId)
                .setInlineKeyboardMarkup(buttonService.createVerticalMenu(btns))
                .setText(messageText)
                .build().createSendMessage();
        stateService.setState(user, FOLDER_DELETE_WAIT_NAME);
        return Arrays.asList(deleteMessage, chooseContact);
    }

    private List<PartialBotApiMethod> folderShow(User user, Update update) {
        val folderList = (List<Folder>) folderRepository.getFoldersByIsDelete(false);
        val messageText = new StringBuilder();
        messageText.append(folderList.size() == 0 ? "Папки отсутствуют" : "Доступные папки:").append(NEW_LINE);
        for (int i = 0; i < folderList.size(); ++i) {
            messageText.append(i + 1).append(") ").append(folderList.get(i).getName()).append(NEW_LINE);
        }
        stateService.setState(user, State.FREE);
        return Arrays.asList(
                EditMessageTextWrap.init()
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                        .setText(messageText.toString())
                        .build().createEditMessageText());
    }

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
        folder.setIsDelete(false);
        folderRepository.save(folder);
        stateService.setState(user, State.FREE);
        return Arrays.asList(
                SendMessageWrap.init()
                        .setChatIdLong(update.getMessage().getChatId())
                        .setText(FOLDER_ADD_OK_SAVE + SPACE + folder.getName())
                        .build().createSendMessage());
    }
}
