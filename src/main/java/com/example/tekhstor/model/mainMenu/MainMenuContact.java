package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.enums.State;
import com.example.tekhstor.model.comparator.ContactComparator;
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

import java.util.*;
import java.util.stream.Collectors;

import static com.example.tekhstor.constant.Constant.NEW_LINE;
import static com.example.tekhstor.constant.Constant.SPACE;
import static com.example.tekhstor.enums.State.*;

@Component
@Slf4j
public class MainMenuContact extends MainMenu {
    final String MENU_NAME = "/contact";

    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private FolderRepository folderRepository;

    private Map<User, Folder> folderTmp = new HashMap();

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
            case CONTACT_ADD_WAIT_CHAT_ID:
                return waitChatIdLogic(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> freeLogic(User user, Update update) {
        val btns = new HashMap<State, String>();
        btns.put(CONTACT_SHOW, "Показать контакты");
        btns.put(CONTACT_ADD, "Добавить контакт");
        btns.put(CONTACT_DELETE, "Удалить контакт");
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
        return errorMessageDefault(update);
    }

    private final String CONTACT_ADD_TEXT = "Режим добавления нового контакта.\nВыберите папку";
    private final String CONTACT_ADD_OK_SAVE = "Новый контакт успешно сохранен";

    private List<PartialBotApiMethod> waitChatIdLogic(User user, Update update) {
        val messageText = update.getMessage().getText();
        try {
            Long.parseLong(messageText);
        } catch (Exception ex) {
            val message = "Ошибка! Некорректный Chat_id контакта:" + messageText + "\nВведите chat_id контакта:";
            log.error(message);
            return errorMessage(update, message);
        }
        val contact = new Contact();
        contact.setChatId(update.getMessage().getText());
        contact.setFolder(folderTmp.get(user));
        contactRepository.save(contact);
        folderTmp.remove(user);
        stateService.setState(user, State.FREE);
        return Arrays.asList(
                SendMessageWrap.init()
                        .setChatIdLong(update.getMessage().getChatId())
                        .setText(CONTACT_ADD_OK_SAVE)
                        .build().createSendMessage());
    }

    private List<PartialBotApiMethod> waitFolderNameLogic(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        stateService.setState(user, State.CONTACT_ADD_WAIT_CHAT_ID);
        folderTmp.put(user, folderRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get());
        return Arrays.asList(EditMessageTextWrap.init()
                .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setText("Введите chat_id контакта:")
                .build().createEditMessageText());
    }

    private List<PartialBotApiMethod> contactAdd(User user, Update update) {
        val btns = new HashMap<String, String>();
        val folderList = (List<Folder>) folderRepository.findAll();
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
        val contactList = (List<Contact>) contactRepository.findAll();
        val messageText = new StringBuilder("Контакты");
        messageText.append(contactList.size() == 0 ? " отсутствуют\n" : ":\n");
        //TODO contactList.stream().sorted(new ContactComparator()).toList();
        // contactList.stream().flatMap(e -> )
        //.sorted(Comparator.comparing(msg -> msg.getId()))
        for (int i = 0; i < contactList.size(); ++i) {
            messageText.append(i + 1).append(") ")
                    .append(contactList.get(i).getFolder().getName()).append(" : ")
                    .append(contactList.get(i).getChatId()).append(NEW_LINE);
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
