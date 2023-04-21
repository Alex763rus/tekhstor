package com.example.tekhstor.model;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.util.Map;
import java.util.Set;

@Getter
@Setter
@ToString
public class WhiteListUser {
    private Map<Long, String> whiteListChatsID;

}
