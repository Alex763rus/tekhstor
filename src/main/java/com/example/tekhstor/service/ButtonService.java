package com.example.tekhstor.service;

import com.example.tekhstor.enums.State;
import lombok.extern.slf4j.Slf4j;
import lombok.val;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
public class ButtonService {

    public InlineKeyboardMarkup createVerticalMenu(Map<String, String> menuDescription) {
        val inlineKeyboardMarkup = new InlineKeyboardMarkup();
        val rows = new ArrayList<List<InlineKeyboardButton>>();

        for (val entry : menuDescription.entrySet()) {
            List<InlineKeyboardButton> rowInline = new ArrayList<>();
            val btn = new InlineKeyboardButton();
            btn.setText(entry.getValue().toString());
            btn.setCallbackData(entry.getKey().toString());
            rowInline.add(btn);
            rows.add(rowInline);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }

    public InlineKeyboardMarkup createVerticalMenuState(Map<State, String> menuDescription) {
        val inlineKeyboardMarkup = new InlineKeyboardMarkup();
        val rows = new ArrayList<List<InlineKeyboardButton>>();
        for (Map.Entry entry : menuDescription.entrySet()) {
            val rowInline = new ArrayList<InlineKeyboardButton>();
            val btn = new InlineKeyboardButton();
            btn.setText(entry.getValue().toString());
            btn.setCallbackData(entry.getKey().toString());
            rowInline.add(btn);
            rows.add(rowInline);
        }
        inlineKeyboardMarkup.setKeyboard(rows);
        return inlineKeyboardMarkup;
    }
}
