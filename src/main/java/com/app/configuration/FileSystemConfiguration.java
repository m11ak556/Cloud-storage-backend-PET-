package com.app.configuration;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.nio.file.Path;

@Getter
@ConfigurationProperties("storage")
public class FileSystemConfiguration {
    public Path getRootPath() {
        Path rootPath = Path.of(rootDirectory);
        return rootPath;
    }

    private final String rootDirectory = "src/files";
    private final String trashbinDirectory = ".trashbin";
    private final String tmpDirectory = ".tmp";
}
