package com.example.projectwebmovie.enums;


public enum MovieVersion {
    _2D("2D"),
    _3D("3D"),
    _IMAX("IMAX");

    private final String value;

    MovieVersion(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static MovieVersion fromValue(String value) {
        for (MovieVersion mv : MovieVersion.values()) {
            if (mv.getValue().equalsIgnoreCase(value)) {
                return mv;
            }
        }
        throw new IllegalArgumentException("Invalid MovieVersion: " + value);
    }

}
