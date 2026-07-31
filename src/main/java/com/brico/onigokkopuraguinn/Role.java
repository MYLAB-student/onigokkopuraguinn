package com.brico.onigokkopuraguinn;

public enum Role {
    POLICE("警察", "§9"),
    THIEF("泥棒", "§c");

    private final String displayName;
    private final String color;

    Role(String displayName, String color) {
        this.displayName = displayName;
        this.color = color;
    }

    public String displayName() {
        return displayName;
    }

    public String coloredName() {
        return color + displayName;
    }
}
