package com.app.services;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import org.apache.tomcat.util.http.fileupload.FileUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.util.FileSystemUtils;
import org.springframework.web.multipart.MultipartFile;

import javax.naming.spi.DirectoryManager;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;

@Service
public class FileSystemService implements IFileSystemService {

    @Autowired
    public FileSystemService(FileSystemConfiguration uploadConfiguration) {
        this.rootPath = Paths.get(uploadConfiguration.getRootDirectory());
    }

    @Override
    public void saveFile(MultipartFile file, String destination) {
        String fileName = file.getOriginalFilename();
        Path saveTo = getDirectoryPath(destination)
                .resolve(fileName)
                .normalize();

        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, saveTo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return;
        }
    }

    @Override
    public Resource getFile(String fileLocation) {
        Path pathToFile = getFilePath(fileLocation);

        try {
            Resource file = new UrlResource(pathToFile.toUri());

            if (!file.exists())
                throw new FileNotFoundException();
            return file;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteFile(String fileLocation) {
        Path pathToFile = getFilePath(fileLocation);
        try {
            if (Files.notExists(pathToFile))
                throw new FileNotFoundException();

            Files.delete(pathToFile);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void moveFile(String source, String destination) {
        Path sourcePath = getFilePath(source);
        Path destinationPath = getFilePath(destination);
        try {
            Files.move(sourcePath, destinationPath);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void createDirectory(String directoryName) {
        Path pathToDirectory = getDirectoryPath(directoryName);
        try {
            Files.createDirectories(pathToDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDirectory(String directoryLocation) {
        Path pathToDirectory = getDirectoryPath(directoryLocation);
        try {
            FileSystemUtils.deleteRecursively(pathToDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    private final Path rootPath;

    private Path getFilePath(String fileName) {
        return rootPath.resolve(fileName).normalize();
    }

    private Path getDirectoryPath(String directoryName) {
        return rootPath.resolve(directoryName).normalize();
    }
}
