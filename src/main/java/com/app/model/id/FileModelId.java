package com.app.model.id;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

/**
 * Составной первичный ключ модели файлов
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
public class FileModelId implements Serializable {
    private long id;
    private long userId;
}
