package com.app.configuration;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TrashbinInitializer {
    public void initializeTrashbin() {
        Path rootPath = Path.of(fileSystemConfiguration.getRootDirectory());
        Path trashbinPath = rootPath.resolve(fileSystemConfiguration.getTrashbinDirectory()).normalize();
        if (Files.notExists(trashbinPath)) {
            try {
                Files.createDirectory(trashbinPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Autowired
    private FileSystemConfiguration fileSystemConfiguration;
}
