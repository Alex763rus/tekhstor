package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.enums.State;
import com.example.tekhstor.model.jpa.*;
import com.example.tekhstor.model.wpapper.DeleteMessageWrap;
import com.example.tekhstor.model.wpapper.EditMessageTextWrap;
import com.example.tekhstor.model.wpapper.SendMessageWrap;
import com.example.tekhstor.service.RestService;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.json.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static com.example.tekhstor.constant.Constant.NEW_LINE;
import static com.example.tekhstor.enums.State.*;

@Component
@Slf4j
public class MainMenuContact extends MainMenu {
    final String MENU_NAME = "/contact";

    @Autowired
    private ContactRepository contactRepository;
    @Autowired
    private FolderRepository folderRepository;

    @Autowired
    private RestService restService;
    private Map<User, Folder> folderTmp = new HashMap();

    private final String CONTACT_ADD_TEXT = "Режим добавления нового контакта.\nВыберите папку";
    private final String CONTACT_DELETE_TEXT = "Режим удаления контакта.\nВыберите папку";
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
        val contact = contactRepository.getContactByIsDeleteAndUsernameIs(false, update.getMessage().getText());
        contact.setIsDelete(true);
        contactRepository.save(contact);
        return Arrays.asList(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setText("Контакт успешно удален")
                .build().createSendMessage());
    }

    private List<PartialBotApiMethod> contactDeleteWaitFolder(User user, Update update) {
        if (!update.hasCallbackQuery()) {
            return errorMessageDefault(update);
        }
        val answer = new ArrayList<PartialBotApiMethod>();

        val contactList = (List<Contact>) contactRepository.getContactsByIsDelete(false);
        val messageText = new StringBuilder("Контакты");
        messageText.append(contactList.size() == 0 ? " отсутствуют\n" : ":\n");
        for (int i = 0; i < contactList.size(); ++i) {
            val contact = contactList.get(i);
            messageText.append(i + 1).append(") ")
                    .append(contact.getFolder().getName())
                    .append(" : ").append(contact.getChatId())
                    .append(" - ").append(contact.getUsername())
                    .append(" - ").append(contact.getTitle())
                    .append(NEW_LINE)
            ;
        }
        answer.add(EditMessageTextWrap.init()
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                .setText(messageText.toString())
                .build().createEditMessageText());

        stateService.setState(user, CONTACT_DELETE_WAIT_USER_NAME);
        folderTmp.put(user, folderRepository.findById(Long.parseLong(update.getCallbackQuery().getData())).get());
        answer.add(EditMessageTextWrap.init()
                .setChatIdLong(update.getCallbackQuery().getMessage().getChatId())
                .setMessageId(update.getCallbackQuery().getMessage().getMessageId())
                .setText("Введите username контакта:")
                .build().createEditMessageText());
        return answer;
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
        if (callBackData.equals(String.valueOf(CONTACT_DELETE))) {
            return contactDelete(user, update);
        }
        return errorMessageDefault(update);
    }

    private List<PartialBotApiMethod> contactDelete(User user, Update update) {
        val btns = new HashMap<String, String>();
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
        val messageText = CONTACT_DELETE_TEXT + (folderList.size() == 0 ? " отсутствуют\n" : ":\n");
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
                contact.setUsername(username);
                contact.setIsDelete(false);
                val jsonData = restService.getChatInfo(userService.getApiKey(user), username);
                JSONObject jsonObject = new JSONObject(jsonData);
                contact.setTitle(jsonObject.getString("title"));
                contact.setChatId(String.valueOf(jsonObject.getLong("id")));
                contactRepository.save(contact);
                ++counterSuccessfullySaved;
            } catch (Exception ex) {
                val message = "Ошибка! Некорректный userName контакта:" + username +". Контакт не сохранен!";
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
                .setText("Введите username контакта:")
                .build().createEditMessageText());
    }

    private List<PartialBotApiMethod> contactAdd(User user, Update update) {
        val btns = new HashMap<String, String>();
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
        //TODO contactList.stream().sorted(new ContactComparator()).toList();
        // contactList.stream().flatMap(e -> )
        //.sorted(Comparator.comparing(msg -> msg.getId()))
        for (int i = 0; i < contactList.size(); ++i) {
            val contact = contactList.get(i);
            messageText.append(i + 1).append(") ")
                    .append(contact.getFolder().getName())
                    .append(" : ").append(contact.getChatId())
                    .append(" - ").append(contact.getUsername())
                    .append(" - ").append(contact.getTitle())
                    .append(NEW_LINE)
            ;
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
