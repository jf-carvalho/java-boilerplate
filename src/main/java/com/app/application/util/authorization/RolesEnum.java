package com.app.application.util.authorization;

public enum RolesEnum {
    SUPER("super"),
    COMMON("common user");

    private final String displayName;

    RolesEnum(String displayName) {
        this.displayName = displayName;
    }

    public String displayName() {
        return displayName;
    }

    @Override
    public String toString() {
        return displayName;
    }
}