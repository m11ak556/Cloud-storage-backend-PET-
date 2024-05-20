package com.app.controllers;

import com.app.interfaces.IFileSystemService;
import com.app.model.FileModel;
import com.app.model.FileTypes;
import com.app.model.User;
import com.app.repositories.IFileModelRepository;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Path;
import java.util.Date;

/**
 * Предоставляет методы работы с директориями
 */
@Controller
public class DirectoryManagementController {
    @Autowired
    public DirectoryManagementController(IFileSystemService fileSystemService,
                                         IFileModelRepository fileModelRepository,
                                         IUserRepository userRepository) {
        this.fileSystemService = fileSystemService;
        this.fileModelRepository = fileModelRepository;
        this.userRepository = userRepository;
    }

    /**
     * Создает директорию для указанного пользователя по указанному пути
     * @param userId
     *      Id пользователя, для которого создается директория
     * @param directoryName
     *      Имя директории (не должно содержать символа "/")
     * @param path
     *      Путь создания директории. Необходимо указывать БЕЗ рабочей папки пользователя
     */
    @PostMapping(apiName + "/create")
    @ResponseBody
    public void createDirectory(@RequestParam long userId, String directoryName, String path) {
        try {
            User user = userRepository.findById(userId).orElse(null);
            Path workingDirPath = Path.of(user.getWorkingDirectory());

            // Добавление рабочей папки к пути создания.
            // Это необходимо для корректного создания файла в файловой системе.
            String createAtPath = workingDirPath
                    .resolve(directoryName)
                    .normalize()
                    .toString();

            FileModel directory = new FileModel();
            directory.setUserId(userId);
            directory.setName(directoryName);
            directory.setDateCreated(new Date());
            directory.setType(FileTypes.DIRECTORY);
            directory.setPath(path);

            fileSystemService.createDirectory(createAtPath);
            fileModelRepository.save(directory);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping(apiName + "/delete")
    @ResponseBody
    public void deleteDirectory(@RequestParam String directoryName) {
        try {
            fileSystemService.deleteForce(directoryName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final String apiName = "/directory";
    private final IFileSystemService fileSystemService;
    private final IFileModelRepository fileModelRepository;
    private final IUserRepository userRepository;
}
