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

    /**
     * Сохраняет файл в указанну папку с учетом корневой папки.
     *
     * @param file
     *        Сохраняемый файл.
     *
     * @param destination
     *        Относительный путь сохранения. ДЛЯ СОХРАНЕНИЯ В КОРЕНЬ
     *        СЛЕДУЕТ ОСТАВИТЬ ПУСТЫМ.
     * */
    @Override
    public void saveFile(MultipartFile file, String destination) {
        String fileName = file.getOriginalFilename();
        Path saveTo = getResolvedPath(destination)
                .resolve(fileName)
                .normalize();

        try (InputStream inputStream = file.getInputStream()) {
            createDirectory(destination);
            Files.copy(inputStream, saveTo, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException e) {
            return;
        }
    }

    @Override
    public void saveAllFiles(MultipartFile[] files, String destination) {
        for (MultipartFile file : files)
            saveFile(file, destination);
    }

    @Override
    public Resource getFile(String fileLocation) {
        Path pathToFile = getResolvedPath(fileLocation);

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
        Path pathToFile = getResolvedPath(fileLocation);
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
        Path sourcePath = getResolvedPath(source);
        Path destinationPath = getResolvedPath(destination);
        try {
            Files.move(sourcePath, destinationPath.resolve(sourcePath.getFileName()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void moveFiles(String[] sources, String destination) {
        for (String source : sources)
            moveFile(source, destination);
    }

    @Override
    public void createDirectory(String directoryName) {
        Path pathToDirectory = getResolvedPath(directoryName);
        try {
            Files.createDirectories(pathToDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public void deleteDirectory(String directoryLocation) {
        Path pathToDirectory = getResolvedPath(directoryLocation);
        try {
            FileSystemUtils.deleteRecursively(pathToDirectory);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }
    public Path getResolvedPath(String fileName) {
        return rootPath.resolve(fileName).normalize();
    }
    private final Path rootPath;

}
