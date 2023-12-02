package com.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

public enum FileTypes {
    DOCUMENT ("Документ"),
    AUDIO ("Аудио"),
    VIDEO ("Видео"),
    IMAGE ("Изображение"),
    DIRECTORY ("Папка"),
    OTHER ("Другое");

    private String title;

    FileTypes(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
}
