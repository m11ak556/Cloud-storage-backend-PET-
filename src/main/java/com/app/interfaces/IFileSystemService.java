package com.app.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

public interface IFileSystemService {
    void saveFile(MultipartFile file, String destination);
    void saveAllFiles(MultipartFile[] files, String destination);
    Resource getFile(String fileLocation);
    void deleteFile(String fileLocation);
    void createDirectory(String directoryName);
    /**
     * Принудительно удаляет файл или папку по указанному пути.
     * Папки удаляются рекурсивно.
     *
     * @param directoryLocation
     *        путь к удаляемому файлу или удаляемой папке
    * */
    void deleteDirectory(String directoryLocation);
    void moveFile(String source, String destination);
    Path getResolvedPath(String fileName);
}
