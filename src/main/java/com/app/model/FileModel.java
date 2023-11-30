package com.app.model;

import java.util.Date;

public record FileModel(
        String name,
        Date dateCreated,
        FileTypes type,
        Long size

) {
}
