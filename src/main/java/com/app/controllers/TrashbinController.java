package com.app.controllers;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import com.app.model.FileModel;
import com.app.model.User;
import com.app.model.id.FileModelId;
import com.app.repositories.IFileModelRepository;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.*;

/**
 * Предоставляет методы для работы с корзиной
 */
@Controller
@CrossOrigin("http://localhost:3000")
public class TrashbinController {
    @Autowired
    public TrashbinController(FileSystemConfiguration fileSystemConfiguration,
                              IFileSystemService fileSystemService,
                              IFileModelRepository fileModelRepository,
                              IUserRepository userRepository) {
        trashbinPath = fileSystemConfiguration.getTrashbinDirectory();
        this.fileSystemService = fileSystemService;
        this.fileModelRepository = fileModelRepository;
        this.userRepository = userRepository;
    }

    /**
     * Получает список файлов из корзины указанного пользователя
     * @param userId
     *      Пользователь, корзину которого требуется получить
     */
    @GetMapping(apiName + "/get")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getFiles(@RequestParam long userId) {
        // Задание значений параметров поиска
        FileModel probe = new FileModel();
        probe.setUserId(userId);
        probe.setDeleted(true);

        // Задание параметров поиска
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "size")
                .withMatcher("user_id", exact())
                .withMatcher("is_deleted", exact());

        // Поиск файлов по указанным параметрам
        List<FileModel> fileModels = fileModelRepository.findAll(Example.of(probe, matcher));
        return ResponseEntity.ok(fileModels);
    }

    /**
     * Помещает указанный файл в корзину
     * @param userId
     *      Пользователь, помещающиий файл в корзину
     * @param fileName
     *      Имя помещаемого файла
     * @param filePath
     *      ПУть к файлу, помещаемому в корзину
     */
    @PostMapping(apiName + "/put")
    @ResponseBody
    public void putFile(@RequestParam long userId, String fileName, String filePath) {
        User user = userRepository.findById(userId).orElse(null);

        // Поиск файла по его пути и имени
        FileModel file = fileModelRepository.findByNameAndPath(fileName, filePath).orElse(null);

        // Добавление рабочей директории пользователя к пути начала и конца перемещения
        String source = user.getWorkingDirectory() + "/" + filePath + "/" + fileName;
        String destination = user.getWorkingDirectory() + "/" + trashbinPath;

        file.setDeleted(true);

        fileModelRepository.save(file);
        fileSystemService.moveFile(source, destination);
    }

    /**
     * Восстанавливает указанный файл из корзины в свою изначальную директорию
     * @param userId
     *      Пользователь, файл которого нужно восстановить
     * @param fileId
     *      Id восстанавливаемого файла
     */
    @PutMapping(apiName + "/restore")
    @ResponseBody
    public void restoreFile(@RequestParam long userId, @RequestParam long fileId) {
        User user = userRepository.findById(userId).orElse(null);
        Path workingDirPath = Path.of(user.getWorkingDirectory());

        // Сборка составного ключа для файла
        FileModelId id = new FileModelId(fileId, userId);
        FileModel fileModel = fileModelRepository.findById(id).orElse(null);
        fileModel.setDeleted(false);

        // Добавление рабочей директории пользователя к пути файла в корзине
        String source = workingDirPath
                .resolve(trashbinPath)
                .resolve(fileModel.getName())
                .normalize()
                .toString();

        // Добавление рабочей директории пользователя к изначальному пути файла
        String destination = workingDirPath
                .resolve(fileModel.getPath())
                .normalize()
                .toString();

        fileModelRepository.save(fileModel);
        fileSystemService.moveFile(source, destination);
    }

    /**
     * Безвозвратно удаляет указанный файл
     * @param userId
     *      Пользователь, удаляющий файл
     * @param fileId
     *      Удаляемый файл
     */
    @DeleteMapping(apiName + "/destroy")
    @ResponseBody
    public void destroyFile(@RequestParam long userId, @RequestParam long fileId) {
        User user = userRepository.findById(userId).orElse(null);
        Path workingDirPath = Path.of(user.getWorkingDirectory());

        // Сборка составного ключа для файла
        FileModelId id = new FileModelId(fileId, userId);
        FileModel fileModel = fileModelRepository.findById(id).orElse(null);

        // Добавление рабочей директории пользователя к пути файла в корзине
        String fullPath = workingDirPath
                .resolve(trashbinPath)
                .resolve(fileModel.getName())
                .normalize()
                .toString();

        fileModelRepository.delete(fileModel);
        fileSystemService.deleteFile(fullPath);
    }

    private final String trashbinPath;
    private final String apiName = "/trashbin";
    private final IFileSystemService fileSystemService;
    private final IFileModelRepository fileModelRepository;
    private final IUserRepository userRepository;
}
