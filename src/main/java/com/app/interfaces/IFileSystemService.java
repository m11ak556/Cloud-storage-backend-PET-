package com.app.interfaces;

import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Path;

/**
 * Обеспечивает работу с файлами на уровне файловой системы
 */
public interface IFileSystemService {
    /**
     * Сохраняет файл по указанному пути
     * @param file
     *      Сохраняемый файл
     * @param destination
     *      Путь сохранения БЕЗ имени сохраняемого файла. ДЛЯ СОХРАНЕНИЯ В КОРЕНЬ
     *      ПУТЬ СЛЕДУЕТ ОСТАВЛЯТЬ ПУСТЫМ
     */
    void saveFile(MultipartFile file, String destination);

    /**
     * Сохраняет группу файлов по указанному пути
     * @param files
     *      Сохраняемые файлы
     * @param destination
     *      Пути сохранения БЕЗ имен сохраняемых файлов ДЛЯ СОХРАНЕНИЯ В КОРЕНЬ
     *      ПУТЬ СЛЕДУЕТ ОСТАВЛЯТЬ ПУСТЫМ
     */
    void saveAllFiles(MultipartFile[] files, String destination);

    /**
     * Получает файл по указанному пути
     * @param fileLocation
     *      Путь к файлу
     */
    Resource getFile(String fileLocation);

    /**
     * Удаляет файл по указанному пути
     * @param fileLocation
     *      Путь файла для удаления
     */
    void deleteFile(String fileLocation);

    /**
     * Создает указанную папку (путь должен быть включен в имя)
     * @param directoryName
     *      Имя папки (включаа путь)
     */
    void createDirectory(String directoryName);
    /**
     * Принудительно удаляет файл или папку по указанному пути.
     * Папки удаляются рекурсивно.
     *
     * @param directoryLocation
     *        Путь к удаляемому файлу или удаляемой папке
    * */
    void deleteForce(String directoryLocation);

    /**
     * Перемещает указанный файл в указанную директорию
     * @param source
     *      Исходный путь файла
     * @param destination
     *      Путь перемещения файла БЕЗ указания имени файла
     */
    void moveFile(String source, String destination);

    /**
     * Перемещает группу файлов по указанному пути
     * @param sources
     *      Исходные пути файлов
     * @param destination
     *      Путь перемещения файлов БЕЗ указания имен файлов
     */
    void moveFiles(String[] sources, String destination);

    /**
     * Получает путь, разрешенный относительно корневой директории
     * @param fileName
     *      Имя разрешаемого файла (не может начинаться с "/")
     */
    Path getResolvedPath(String fileName);
}
