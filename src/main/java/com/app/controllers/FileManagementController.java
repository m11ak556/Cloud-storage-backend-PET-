package com.app.controllers;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileModelService;
import com.app.interfaces.IFileSystemService;
import com.app.interfaces.IZipArchiverService;
import com.app.model.FileModel;
import com.app.model.FileTypes;
import com.app.model.User;
import com.app.model.id.FileModelId;
import com.app.repositories.IFileModelRepository;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.*;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.*;

/**
 * Обеспечивает методы работы с файлами в системе
 */
@Controller
public class FileManagementController {
    @Autowired
    public FileManagementController(IFileSystemService fileSystemService,
                                    IFileModelRepository fileModelRepository,
                                    IUserRepository userRepository,
                                    IZipArchiverService zipArchiverService,
                                    FileSystemConfiguration fileSystemConfiguration,
                                    IFileModelService fileModelService) {
        this.fileSystemService = fileSystemService;
        this.fileModelRepository = fileModelRepository;
        this.userRepository = userRepository;
        this.zipArchiverService = zipArchiverService;
        this.tmpDirectory = fileSystemConfiguration.getTmpDirectory();
        this.fileModelService = fileModelService;
    }

    /**
     * Загружает одиночный файл по указанному пути
     * @param file
     *      Загружаемый файл
     * @param userId
     *      Пользователь, осуществляющий загрузку
     * @param destination
     *      Путь загрузки. При указании пустого пути файл загружается в рабочую директорию пользователя
     */
    @PostMapping(apiName + "/upload")
    @ResponseBody
    public void uploadFile(@RequestParam MultipartFile file,
                           @RequestParam long userId,
                           @RequestParam String destination) {
        User user = userRepository.findById(userId).orElse(null);
        Path workingDirectory = Path.of(user.getWorkingDirectory());
        FileModel fileModel = null;

        // Добавляет рабочую директорию пользователя к пути файла.
        // Так файл будет сохранен в папку пользователя, а не в корень программы.
        // Это нужно для файловой системы
        String fullDestination = workingDirectory.resolve(destination)
                .normalize()
                .toString();

        if (!destination.isEmpty()) {
            // Создание модели файла для сохранения его в базу данных
            fileModel = buildFileModel(file, userId, destination);
            // Папу также сохраняем в базу данных как файл
            fetchFolderAndSave(userId, destination);
        }

        fileModelRepository.save(fileModel);
        fileSystemService.saveFile(file, fullDestination);
    }

    /**
     * Загружает группу файлов по указанному пути
     * @param files
     *      Загружаемые файлы
     * @param userId
     *      Пользователь, осуществляющий загрузку
     * @param destination
     *      Путь загрузки. При указании пустого пути файл загружается в рабочую директорию пользователя
     */
    @PostMapping(apiName + "/uploadMultiple")
    @ResponseBody
    public ResponseEntity uploadFiles(@RequestParam MultipartFile[] files,
                           @RequestParam long userId,
                           @RequestParam String destination) {
        User user = userRepository.findById(userId).orElse(null);
        Path workingDirectory = Path.of(user.getWorkingDirectory());

        // Добавляет рабочую директорию пользователя к пути файла.
        // Так файл будет сохранен в папку пользователя, а не в корень программы.
        String fullDestination = workingDirectory.resolve(destination)
                .normalize()
                .toString();

        if (!destination.isEmpty()) {
            // Папу также сохраняем в базу данных как файл
            try {
                fetchFolderAndSave(userId, destination);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        // Создание моделей загруженных файлов для сохранения
        // и в базу данных
        List<FileModel> fileModels = new ArrayList<FileModel>();
        for (MultipartFile file: files) {
//            if (file.getSize() / (1024 * 3) > 4)
//                return ResponseEntity.status(HttpStatus.BAD_REQUEST)
//                        .body("Превышен максимальный размер файла");

            FileModel fileModel = buildFileModel(file, userId, destination);
            fileModels.add(fileModel);
        }

        fileModelRepository.saveAll(fileModels);
        fileSystemService.saveAllFiles(files, fullDestination);

        return ResponseEntity.ok().build();
    }

    /**
     * Скачивает одиночный файл, расположенный по указанному пути
     * @param userId
     *      Пользователь, скачивающий файл
     * @param filePath
     *      Путь к скачиваемому файлу. Указывается БЕЗ рабочей директории пользователя
     */
    @GetMapping(apiName + "/download")
    @ResponseBody
    public ResponseEntity downloadFile(@RequestParam long userId, String filePath) {
        User user = userRepository.findById(userId).orElse(null);

        if (user == null)
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Пользователь не найден");
        
        // Добавление рабочей директории пользователя к пути файла.
        // Так файл будет корректно определен архиватором.
        String fullPath = Path.of(user.getWorkingDirectory())
                .resolve(filePath)
                .normalize()
                .toString();

        Resource resource = null;

        try {
            // Получение файла из файловой системы
            resource = fileSystemService.getFile(fullPath);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Файл не найден");
        }

        // Отправление файла клиенту в пакете Http
        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE,
                        URLConnection.guessContentTypeFromName(resource.getFilename()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filePath=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    /**
     * Скачивает группу файлов, расположенных по указанным путям
     * @param userId
     *      Пользователь, скачивающий файлы
     * @param filePaths
     *      Пути расположения файлов
     */
    @GetMapping(apiName + "/downloadMultiple")
    @ResponseBody
    public ResponseEntity downloadFiles(@RequestParam long userId, String[] filePaths) {
        if (filePaths == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Не указаны пути скачиваемых файлов");
        }

        User user = userRepository.findById(userId).orElse(null);

        // Добавление рабочей директории пользователя к пути временного сохранения.
        // Это обеспечит корректное сохранение временного архива.
        String saveTo = Path.of(user.getWorkingDirectory())
                .resolve(tmpDirectory)
                .normalize()
                .toString();

        try {
            String fullPath;
            for (int i = 0; i < filePaths.length; i++) {
                // Добавление рабочей директории к пути файла.
                fullPath = Path.of(user.getWorkingDirectory())
                        .resolve(filePaths[i])
                        .normalize()
                        .toString();
                // Разрешение пути относительно корневой директории.
                // Так он будет корректно определен архиватором.
                filePaths[i] = fileSystemService.getResolvedPath(fullPath).toString();
            }
            // Архивация файлов
            File zipFile = zipArchiverService.zip(filePaths, saveTo);
            // Получение созданного архива из файловой системы
            Resource resource = fileSystemService.getFile(zipFile.getAbsolutePath());

            // Отправление пользователю архива с файлами в пакете Http
            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE,
                            URLConnection.guessContentTypeFromName(resource.getFilename()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            return ResponseEntity
                    .status(HttpStatus.CONFLICT)
                    .body("Не удалось загрузить файлы");
        }
    }

    /**
     * Получает список файлов по id пользователя. Файлы выбирваются из корневой директории
     */
    @GetMapping(apiName + "/getByUserId")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getFiles(@RequestParam long userId) {
        return getFiles("", userId);
    }

    /**
     * Получает файлы из указанной директории пользователя
     * @param directory
     *      Директория, из которой необходимо получить файлы
     * @param userId
     *      Пользователь, файлы которого требуется получить
     */
    @GetMapping(apiName + "/get")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getFiles(@RequestParam String directory, long userId) {
        // Задание значений параметров поиска
        FileModel probe = new FileModel();
        probe.setUserId(userId);
        probe.setPath(directory);
        probe.setDeleted(false);

        // Задание параметров поиска
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "size")
                .withMatcher("user_id", exact())
                .withMatcher("path", exact())
                .withMatcher("is_deleted", exact());

        // Поиск файлов по заданным параметрам
        List<FileModel> files = fileModelRepository.findAll(Example.of(probe, matcher));
        return ResponseEntity.ok().body(files);
    }

    /**
     * Получение всех файлов указанного пользователя
     * @param userId
     *      Пользователь, файлы которого необходимо получить
     */
    @GetMapping(apiName + "/getAll")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getAllFiles(@RequestParam long userId) {
        // Задание значений параметров поиска
        FileModel probe = new FileModel();
        probe.setUserId(userId);
        probe.setDeleted(false);

        // Задание параметров поиска
        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "size")
                .withMatcher("user_id", exact())
                .withMatcher("is_deleted", exact());

        // Поиск файлов по заданным параметрам
        List<FileModel> files = fileModelRepository.findAll(Example.of(probe, matcher));
        return ResponseEntity.ok().body(files);
    }

    /**
     * Перемещение указанных файлов в указанную директорию
     * @param fileIds
     *      Id перемещаемых файлов
     * @param userId
     *      Пользователь, перемещающий файлы
     * @param destination
     *      Путь перемещения файлов. Необходимо указывать БЕЗ имени перемещаемого файла
     */
    @PutMapping(apiName + "/move")
    @ResponseBody
    public void moveFiles(@RequestParam long[] fileIds,
                            @RequestParam long userId,
                            @RequestParam String destination) {
        User user = userRepository.findById(userId).orElse(null);
        Path workingDirectory = Path.of(user.getWorkingDirectory());

        // Добавляет рабочую директорию пользователя к пути файла.
        String fullDestination = workingDirectory.resolve(destination)
                .normalize()
                .toString();

        // Сборка составного индентификатора файлов
        List<FileModelId> ids = new ArrayList<>();
        for (int i = 0; i < fileIds.length; i++) {
            FileModelId id = new FileModelId(fileIds[i], userId);
            ids.add(id);
        }

        List<FileModel> fileModels = fileModelRepository.findAllById(ids);
        String[] sources = new String[fileModels.size()];

        String fullSource = "";
        int sourceIdx = 0;

        // Получение исходный путей файлов
        for (FileModel fileModel : fileModels) {
            // Добавление рабочей папки пользователя к пути файла
            fullSource = workingDirectory
                    .resolve(fileModel.getPath())
                    .resolve(fileModel.getName())
                    .normalize()
                    .toString();
            sources[sourceIdx] = fullSource;
            fileModel.setPath(destination);

            sourceIdx++;
        }

        fileModelRepository.saveAll(fileModels);
        fileSystemService.moveFiles(sources, fullDestination);
    }

    private final IFileSystemService fileSystemService;
    private final IFileModelRepository fileModelRepository;
    private final IUserRepository userRepository;
    private final IZipArchiverService zipArchiverService;
    private final IFileModelService fileModelService;
    private final String apiName = "/files";
    private final String tmpDirectory;

    /**
     * Получает путь к указанной директории
     * @param pathWithFolder
     *      Полный путь к папке (с ее именем)
     */
    private String getPathToFolder(String pathWithFolder)
    {
        String pathToFolder = "";
        int endIndex = pathWithFolder.lastIndexOf("/");
        if (endIndex > 0)
            pathToFolder = pathWithFolder.substring(0, endIndex);
        return pathToFolder;
    }

    /**
     * Получает родительскую папку из указанного пути сохранения файла
     * и сохраняет ее в базе данных
     * @param userId
     *      Пользователь сохраняющий файлы
     * @param destination
     *      Путь сохранения файла БЕЗ рабочей директроии пользователя
     */
    private void fetchFolderAndSave(long userId, String destination)
    {
        if (destination.isEmpty())
            return;

        String directoryName = StringUtils.getFilename(destination);
        String pathToFolder = getPathToFolder(destination);

        FileModel folder = buildFileModelFromDirectory(directoryName, userId, pathToFolder);
        fileModelRepository.save(folder);
    }

    /**
     * Создает модель файла из указанного файла
     * @param file
     *      Файл, модель которого требуется создать
     * @param userId
     *      Пользователь, создающий модель
     * @param path
     *      Путь файла в хранилище (куда будет сохранен) БЕЗ рабочей директории пользователя
     */
    private FileModel buildFileModel(MultipartFile file, long userId, String path) {
        FileModel fileModel = new FileModel();

        String fileName = file.getOriginalFilename();
        // Определение типа файла
        FileTypes fileType = fileModelService.guessFileType(fileName);

        fileModel.setName(fileName);
        fileModel.setPath(path);
        fileModel.setDateCreated(new Date());
        fileModel.setUserId(userId);
        fileModel.setSize(file.getSize());
        fileModel.setType(fileType);

        return fileModel;
    }

    /**
     * Создает модель файла из указанной директории
     * @param directoryName
     *      Имя директории, модель которой требуется создать
     * @param userId
     *      Пользователь, создающий директорию
     * @param path
     *      Путь директори в хранилище (куда она будет сохранена) БЕЗ рабочей директории пользователя
     */
    private FileModel buildFileModelFromDirectory(String directoryName, long userId, String path)
    {
        FileModel fileModel = new FileModel();
        fileModel.setName(directoryName);
        fileModel.setPath(path);
        fileModel.setDateCreated(new Date());
        fileModel.setUserId(userId);
        fileModel.setType(FileTypes.DIRECTORY);

        return fileModel;
    }
}
