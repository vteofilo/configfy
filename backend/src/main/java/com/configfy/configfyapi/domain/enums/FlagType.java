package com.configfy.configfyapi.domain.enums;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum FlagType {
    BOOLEAN("boolean", "Boolean flag (true/false)"),
    STRING("string", "String value flag"),
    NUMBER("number", "Numeric value flag"),
    JSON("json", "JSON object flag");

    private final String code;
    private final String description;

    public static FlagType fromCode(String code) {
        for (FlagType type : values()) {
            if (type.code.equalsIgnoreCase(code)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Unknown flag type: " + code);
    }
}
