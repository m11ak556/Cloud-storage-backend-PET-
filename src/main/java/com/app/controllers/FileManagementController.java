package com.app.controllers;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import com.app.interfaces.IZipArchiverService;
import com.app.model.FileModel;
import com.app.model.FileTypes;
import com.app.model.User;
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

@Controller
public class FileManagementController {
    @Autowired
    public FileManagementController(IFileSystemService fileSystemService,
                                    IFileModelRepository fileModelRepository,
                                    IUserRepository userRepository,
                                    IZipArchiverService zipArchiverService,
                                    FileSystemConfiguration fileSystemConfiguration) {
        this.fileSystemService = fileSystemService;
        this.fileModelRepository = fileModelRepository;
        this.userRepository = userRepository;
        this.zipArchiverService = zipArchiverService;
        this.tmpDirectory = fileSystemConfiguration.getTmpDirectory();
    }

    // destination может быть пустой строкой.
    // Это означает сохранение в рабочую папку.
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

            fileModel = buildFileModel(file, userId, destination);
            // Папу также сохраняем в базу данных как файл
            fetchFolderAndSave(userId, destination);
        }

        fileModelRepository.save(fileModel);
        fileSystemService.saveFile(file, fullDestination);
    }

    @PostMapping(apiName + "/uploadMultiple")
    @ResponseBody
    public void uploadFiles(@RequestParam MultipartFile[] files,
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

        List<FileModel> fileModels = new ArrayList<FileModel>();
        for (MultipartFile file: files) {
            FileModel fileModel = buildFileModel(file, userId, destination);
            fileModels.add(fileModel);
        }


        fileModelRepository.saveAll(fileModels);
        fileSystemService.saveAllFiles(files, fullDestination);
    }

    @GetMapping(apiName + "/download")
    @ResponseBody
    public ResponseEntity downloadFile(@RequestParam long userId, String filePath) {
        User user = userRepository.findById(userId).orElse(null);
        String fullPath = Path.of(user.getWorkingDirectory())
                .resolve(filePath)
                .normalize()
                .toString();

        Resource resource = null;

        try {
            resource = fileSystemService.getFile(fullPath);
        } catch (RuntimeException e) {
            e.printStackTrace();
            return ResponseEntity
                    .status(HttpStatus.BAD_REQUEST)
                    .body("File not found");
        }

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_TYPE,
                        URLConnection.guessContentTypeFromName(resource.getFilename()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filePath=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping(apiName + "/downloadMultiple")
    @ResponseBody
    public ResponseEntity downloadFiles(@RequestParam long userId, String[] filePaths) {
        User user = userRepository.findById(userId).orElse(null);
        String saveTo = Path.of(user.getWorkingDirectory())
                .resolve(tmpDirectory)
                .normalize()
                .toString();

        try {
            String fullPath;
            for (int i = 0; i < filePaths.length; i++) {
                fullPath = Path.of(user.getWorkingDirectory())
                        .resolve(filePaths[i])
                        .normalize()
                        .toString();
                filePaths[i] = fileSystemService.getResolvedPath(fullPath).toString();
            }
            File zipFile = zipArchiverService.zip(filePaths, saveTo);
            Resource resource = fileSystemService.getFile(zipFile.getAbsolutePath());

            return ResponseEntity.ok()
                    .header(HttpHeaders.CONTENT_TYPE,
                            URLConnection.guessContentTypeFromName(resource.getFilename()))
                    .header(HttpHeaders.CONTENT_DISPOSITION,
                            "attachment; filename=\"" + resource.getFilename() + "\"")
                    .body(resource);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    @GetMapping(apiName + "/getByUserId")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getFiles(@RequestParam long userId) {
        return getFiles("", userId);
    }

    @GetMapping(apiName + "/get")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getFiles(@RequestParam String directory, long userId) {
        FileModel probe = new FileModel();
        probe.setUserId(userId);
        probe.setPath(directory);
        probe.setDeleted(false);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "size")
                .withMatcher("user_id", exact())
                .withMatcher("path", exact())
                .withMatcher("is_deleted", exact());

        List<FileModel> files = fileModelRepository.findAll(Example.of(probe, matcher));
        return ResponseEntity.ok().body(files);
    }

    @GetMapping(apiName + "/getAll")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getAllFiles(@RequestParam long userId) {
        FileModel probe = new FileModel();
        probe.setUserId(userId);
        probe.setDeleted(false);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "size")
                .withMatcher("user_id", exact())
                .withMatcher("is_deleted", exact());

        List<FileModel> files = fileModelRepository.findAll(Example.of(probe, matcher));
        return ResponseEntity.ok().body(files);
    }

    @DeleteMapping(apiName + "/delete")
    @ResponseBody
    public void deleteFile(@RequestParam String filePath) {
        try {
            fileSystemService.deleteFile(filePath);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final IFileSystemService fileSystemService;
    private final IFileModelRepository fileModelRepository;
    private final IUserRepository userRepository;
    private final IZipArchiverService zipArchiverService;
    private final String apiName = "/files";
    private final String tmpDirectory;
    private String getPathToFolder(String destination)
    {
        String pathToFolder = "";
        int endIndex = destination.lastIndexOf("/");
        if (endIndex > 0)
            pathToFolder = destination.substring(0, endIndex);
        return pathToFolder;
    }

    private void fetchFolderAndSave(long userId, String destination)
    {
        if (destination.isEmpty())
            return;

        String directoryName = StringUtils.getFilename(destination);

        String pathToFolder = getPathToFolder(destination);
        FileModel folder = buildFileModelFromDirectory(directoryName, userId, pathToFolder);
        fileModelRepository.save(folder);
    }

    private FileModel buildFileModel(MultipartFile file, long userId, String path) {
        FileModel fileModel = new FileModel();
        fileModel.setName(file.getOriginalFilename());
        fileModel.setPath(path);
        fileModel.setDateCreated(new Date());
        fileModel.setUserId(userId);
        fileModel.setSize(file.getSize());
        fileModel.setType(FileTypes.OTHER);

        return fileModel;
    }

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
