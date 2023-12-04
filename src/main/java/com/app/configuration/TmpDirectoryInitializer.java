package com.app.configuration;

import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class TmpDirectoryInitializer {

    public void initializeTmpDirectory() {
        Path tmpPath = fileSystemConfiguration.getTmpPath();
        if (Files.notExists(tmpPath)) {
            try {
                Files.createDirectory(tmpPath);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    @Autowired
    private FileSystemConfiguration fileSystemConfiguration;
}
