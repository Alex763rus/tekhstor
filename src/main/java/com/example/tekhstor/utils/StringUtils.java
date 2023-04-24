package com.example.tekhstor.utils;

import lombok.Getter;
import lombok.experimental.SuperBuilder;

import static com.example.tekhstor.constant.StringConstant.SHIELD;

@Getter
@SuperBuilder(setterPrefix = "set", builderMethodName = "init", toBuilder = true)
public class StringUtils {

    private static final String[] shieldingSimbols = {"_", "*", "[", "]", "(", ")", "~", "`", ">", "#", "+", " -", "=", "|", "{", "}", ".", "!"};

    public static String getShield(String source) {
        for (String shieldingSimbol : shieldingSimbols) {
            source = source.replace(shieldingSimbol, SHIELD + shieldingSimbol);
        }
        return source;
    }

}
