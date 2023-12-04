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

    public Path getTrashbinPath() {
        Path rootPath = getRootPath();
        Path trashbinPath = rootPath.resolve(trashbinDirectory).normalize();

        return trashbinPath;
    }

    public Path getTmpPath() {
        Path rootPath = getRootPath();
        Path tmpPath = rootPath.resolve(tmpDirectory).normalize();

        return tmpPath;
    }

    private final String rootDirectory = "src/files";
    private final String trashbinDirectory = ".trashbin";

    private final String tmpDirectory = ".tmp";
}
