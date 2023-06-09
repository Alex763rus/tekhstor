package com.example.tekhstor.model.mainMenu;

import com.example.tekhstor.model.jpa.*;
import com.example.tekhstor.model.wpapper.EditMessageTextWrap;
import com.example.tekhstor.model.wpapper.SendMessageWrap;
import com.example.tekhstor.utils.StringUtils;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Component;
import org.telegram.telegrambots.meta.api.methods.PartialBotApiMethod;
import org.telegram.telegrambots.meta.api.objects.Update;

import java.util.*;

import static com.example.tekhstor.constant.StringConstant.NEW_LINE;
import static com.example.tekhstor.constant.StringConstant.SPACE;
import static com.example.tekhstor.enums.State.*;

@Component
@Slf4j
public class MainMenuFolderMessage extends MainMenu {
    final String MENU_NAME = "/folder_message";

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
        val messagesError = new StringBuilder();
        int countSuccessSendMessages = 0;
        for (Contact contact : contacts) {
            try {
                restService.sendMessage(userService.getApiKey(user), contact.getChatId(), message);
                ++countSuccessSendMessages;
            } catch (Exception ex) {
                val messageText = new StringBuilder();
                messageText.append(contact.getFolder().getName()).append(SPACE)
                        .append(StringUtils.getShield(contact.getUsername())).append(":")
                        .append(ex.getMessage()).append(NEW_LINE);
                messagesError.append(messageText);
                log.error("Ошибка во время отправки сообщения:" + messageText);
            }
        }
        val answerText = new StringBuilder("Успешно отправлено сообщений: ");
        answerText.append(countSuccessSendMessages).append(NEW_LINE);
        answerText.append(messagesError);
        val answer = new ArrayList<PartialBotApiMethod>();
        answer.add(SendMessageWrap.init()
                .setChatIdLong(update.getMessage().getChatId())
                .setText(answerText.toString())
                .build().createSendMessage());
        answer.add(freeLogic(user, update).get(0));
        return answer;
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
        val btns = new LinkedHashMap<String, String>();
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
