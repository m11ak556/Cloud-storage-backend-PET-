package com.app.configuration;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("storage")
public class FileSystemConfiguration {
    public String getRootDirectory() {
        return rootDirectory;
    }
    public String getTrashbinDirectory() { return trashbinDirectory; }
    private final String rootDirectory = "src/files";
    private final String trashbinDirectory = ".trashbin";
}
