package com.bil.bilmobileads.entity;

public enum ADFormat {
    HTML("html"), // simple
    VAST("vast"); // video

    private String type;

    ADFormat(String type) {
        this.type = type;
    }
}
