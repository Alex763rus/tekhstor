package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.enums.State;
import com.example.tekhstor.model.jpa.*;
import com.example.tekhstor.model.wpapper.DeleteMessageWrap;
import com.example.tekhstor.model.wpapper.EditMessageTextWrap;
import com.example.tekhstor.model.wpapper.SendMessageWrap;
import com.example.tekhstor.service.ExcelService;
import com.example.tekhstor.service.RestService;
import com.example.tekhstor.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.apache.poi.util.StringUtil;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.methods.send.SendDocument;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;
import java.util.stream.Collectors;

import static com.example.tekhstor.constant.StringConstant.NEW_LINE;
import static com.example.tekhstor.constant.StringConstant.STAR;
import static com.example.tekhstor.enums.State.*;

@Component
@Slf4j
public class MainMenuContact extends MainMenu {
    final String MENU_NAME = "/contact";

    private Map<User, Folder> folderTmp = new HashMap();

    private final String CONTACT_ADD_TEXT = "Режим добавления нового контакта." + NEW_LINE + "Выберите папку";
    private final String CONTACT_DELETE_TEXT = "Режим удаления контакта." + NEW_LINE + "Выберите папку";
    private final String CONTACT_ADD_OK_SAVE = "Контакты успешно сохранены:";

    @Override
    public String getMenuName() {
        return MENU_NAME;
    }

    @Override
    public String getDescription() {
        return "Контакты";
    }

    @Override
    public List<PartialBotApiMethod> menuRun(User user, Update update) {
        switch (stateService.getState(user)) {
            case FREE:
                return freeLogic(user, update);
            case CONTACT_MAIN:
                return contactMain(user, update);
            case CONTACT_ADD_WAIT_FOLDER:
                return waitFolderNameLogic(user, update);
            case CONTACT_ADD_WAIT_USER_NAME:
                return waitUserNameLogic(user, update);
            case CONTACT_DELETE_WAIT_FOLDER:
                return contactDeleteWaitFolder(user, update);
            case CONTACT_DELETE_WAIT_USER_NAME:
                return deleteContact(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> deleteContact(User user, Update update) {
        val username = update.getMessage().getText();
        val folder = folderTmp.get(user);
        val contacts = contactRepository.getContactByIsDeleteAndUsernameIs(false, username);
        for (Contact contact : contacts) {
            if (contact.getFolder().equals(folder)) {
                contact.setIsDelete(true);
                contactRepository.save(contact);
                return Arrays.asList(SendMessageWrap.init()
                        .setChatIdLong(update.getMessage().getChatId())
                        .setText("Контакт успешно удален: " + StringUtils.getShield(contact.getUsername()))
                        .build().createSendMessage());
            }
        }
        return Arrays.asList(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setText("Введенный контакт не найден: " + StringUtils.getShield(username) + " в папке: " + folder.getName())
                .build().createSendMessage());
    }

    private List<PartialBotApiMethod> contactDeleteWaitFolder(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        stateService.setState(user, CONTACT_DELETE_WAIT_USER_NAME);
        folderTmp.put(user, folderRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get());
        return Arrays.asList(EditMessageTextWrap.init()
                .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setText("Введите username контакта:")
                .build().createEditMessageText());
    }

    private List<PartialBotApiMethod> freeLogic(User user, Update update) {
        val btns = new LinkedHashMap<State, String>();
        btns.put(CONTACT_SHOW, "Показать контакты");
        btns.put(CONTACT_ADD, "Добавить контакт");
        btns.put(CONTACT_DELETE, "Удалить контакт");
        btns.put(CONTACT_TO_EXCEL, "Выгрузить контакты");
        stateService.setState(user, CONTACT_MAIN);

        return Arrays.asList(
                SendMessageWrap.init()
                        .setChatIdLong(update.getMessage().getChatId())
                        .setText("Выберите режим работы с контактами:")
                        .setInlineKeyboardMarkup(buttonService.createVerticalMenuState(btns))
                        .build().createSendMessage());
    }

    private List<PartialBotApiMethod> contactMain(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        val callBackData = update.getCallbackQuery().getData();
        if (callBackData.equals(String.valueOf(CONTACT_SHOW))) {
            return contactShow(user, update);
        }
        if (callBackData.equals(String.valueOf(CONTACT_ADD))) {
            return contactAdd(user, update);
        }
        if (callBackData.equals(String.valueOf(CONTACT_DELETE))) {
            return contactDelete(user, update);
        }
        if (callBackData.equals(String.valueOf(CONTACT_TO_EXCEL))) {
            return contactToExcel(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> contactToExcel(User user, Update update) {
        val contaList = contactRepository.getContactsByIsDelete(false);
        List<List<String>> excelData = new ArrayList<>();
        excelData.add(Arrays.asList("№", "Contact ID:", "Folder:", "ChatId:", "Username:"));
        for (int i = 0; i < contaList.size(); ++i) {
            val contact = contaList.get(i);
            excelData.add(
                    Arrays.asList(
                            String.valueOf(i + 1)
                            , String.valueOf(contact.getContactId())
                            , contact.getFolder().getName()
                            , contact.getChatId()
                            , contact.getUsername()
                    )
            );
        }
        SendDocument sendDocument = new SendDocument();
        sendDocument.setDocument(excelService.createExcelDocument("Склад", excelData));
        sendDocument.setChatId(String.valueOf(update.getCallbackQuery().getMessage().getChatId()));
        stateService.setState(user, State.FREE);
        return Arrays.asList(sendDocument);
    }

    private List<PartialBotApiMethod> contactDelete(User user, Update update) {
        val btns = new LinkedHashMap<String, String>();
        val folderList = (List<Folder>) folderRepository.getFoldersByIsDelete(false);
        val chatId = update.getCallbackQuery().getMessage().getChatId();
        val deleteMessage = DeleteMessageWrap.init()
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setChatIdLong(chatId)
                .build().createDeleteMessage();
        if (folderList.size() == 0) {
            val errorAnswer = errorMessage(update, "Папки не найдены");
            return Arrays.asList(deleteMessage, errorAnswer.get(0));
        }
        val messageText = CONTACT_DELETE_TEXT + (folderList.size() == 0 ? " отсутствуют" : ":") + NEW_LINE;
        for (int i = 0; i < folderList.size(); ++i) {
            btns.put(String.valueOf(folderList.get(i).getFolderId()), folderList.get(i).getName());
        }
        val chooseContact = SendMessageWrap.init()
                .setChatIdLong(chatId)
                .setInlineKeyboardMarkup(buttonService.createVerticalMenu(btns))
                .setText(messageText)
                .build().createSendMessage();
        stateService.setState(user, CONTACT_DELETE_WAIT_FOLDER);
        return Arrays.asList(deleteMessage, chooseContact);
    }

    private List<PartialBotApiMethod> waitUserNameLogic(User user, Update update) {
        val messageText = update.getMessage().getText();
        if (!messageText.contains("@")) {
            val message = "Ошибка! Некорректный userName контакта:" + messageText + ".\nОтсутствует \"@\"\nВведите username контакта:";
            log.error(message);
            return errorMessage(update, message);
        }
        if (messageText.contains("https:") || messageText.contains("t.me:/")) {
            val message = "Ошибка! Контакт не должен содержать https и t.me:" + messageText + ".\nВведите username контакта:";
            log.error(message);
            return errorMessage(update, message);
        }
        val usernames = update.getMessage().getText().split(",");
        val folder = folderTmp.get(user);
        int counterSuccessfullySaved = 0;
        int counterFaledSaved = 0;
        val answer = new ArrayList<PartialBotApiMethod>();
        for (String username : usernames) {
            try {
                val contact = new Contact();
                contact.setFolder(folder);
                contact.setUsername(username.trim());
                contact.setIsDelete(false);
                val jsonData = restService.getChatInfo(userService.getApiKey(user), username);
                JSONObject jsonObject = new JSONObject(jsonData);
                contact.setChatId(String.valueOf(jsonObject.getLong("id")));
                contactRepository.save(contact);
                ++counterSuccessfullySaved;
            } catch (Exception ex) {
                val message = "Ошибка! Некорректный userName контакта:" + username + ". Контакт не сохранен!";
                log.error(message);
                answer.add(errorMessage(update, message).get(0));
                ++counterFaledSaved;
            }

        }
        folderTmp.remove(user);
        stateService.setState(user, State.FREE);

        answer.add(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setText(CONTACT_ADD_OK_SAVE + counterSuccessfullySaved
                        + NEW_LINE + "Не сохранено:" + counterFaledSaved
                )
                .build().createSendMessage());
        return answer;
    }

    private List<PartialBotApiMethod> waitFolderNameLogic(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        stateService.setState(user, State.CONTACT_ADD_WAIT_USER_NAME);
        folderTmp.put(user, folderRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get());
        return Arrays.asList(EditMessageTextWrap.init()
                .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setText("Введите username одного или нескольких контактов через запятую, например: @test1,@test2")
                .build().createEditMessageText());
    }

    private List<PartialBotApiMethod> contactAdd(User user, Update update) {
        val btns = new LinkedHashMap<String, String>();
        val folderList = (List<Folder>) folderRepository.getFoldersByIsDelete(false);
        val messageText = CONTACT_ADD_TEXT + (folderList.size() == 0 ? " отсутствуют\n" : ":\n");
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
        stateService.setState(user, CONTACT_ADD_WAIT_FOLDER);
        return Arrays.asList(deleteMessage, chooseContact);
    }

    private List<PartialBotApiMethod> contactShow(User user, Update update) {
        val contactList = (List<Contact>) contactRepository.getContactsByIsDelete(false);
        val messageText = new StringBuilder("Контакты");
        messageText.append(contactList.size() == 0 ? " отсутствуют\n" : ":\n");
        val folderContacts = contactList.stream().collect(Collectors.groupingBy(Contact::getFolder));
        for (Map.Entry<Folder, List<Contact>> folderContact : folderContacts.entrySet()) {
            messageText.append(STAR).append(folderContact.getKey().getName()).append(STAR).append(NEW_LINE);
            for (Contact contact : folderContact.getValue()) {
                messageText.append(StringUtils.getShield(contact.getUsername())).append(NEW_LINE);
            }
            messageText.append(NEW_LINE);
        }
        stateService.setState(user, State.FREE);
        return Arrays.asList(
                EditMessageTextWrap.init()
                        .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                        .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                        .setText(messageText.toString())
                        .build().createEditMessageText());
    }
}
