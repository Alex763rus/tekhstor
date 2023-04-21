package com.example.tekhstor.model;

import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
public class WhiteListUser {
    private Set<Long> whiteListChatsID;

}
