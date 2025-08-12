package com.example.projectwebmovie.enums;

public enum MovieStatus {
    UPCOMING("UPCOMING"),
    ENDED("ENDED"),
    SHOWING("SHOWING");

    private String displayName;

    MovieStatus(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}
