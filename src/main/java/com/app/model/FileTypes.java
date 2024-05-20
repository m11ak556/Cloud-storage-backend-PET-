package com.app.model;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.Data;

public enum FileTypes {
    DOCUMENT ("Документ"),
    SPREADSHEET("Электронная таблица"),
    PRESENTATION("Презентация"),
    TEXT("Текстовый файл"),
    AUDIO ("Аудио"),
    VIDEO ("Видео"),
    IMAGE ("Изображение"),
    COMPRESSED("Архив"),
    DIRECTORY ("Папка"),
    OTHER ("Другое");

    private String title;

    FileTypes(String title) {
        this.title = title;
    }

    public String getTitle() { return title; }
}
