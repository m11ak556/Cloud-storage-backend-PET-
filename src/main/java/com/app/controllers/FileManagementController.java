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

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;

@Controller
//@CrossOrigin("http://localhost:3000")
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

    @PostMapping(apiName + "/upload")
    @ResponseBody
    public void uploadFile(@RequestParam MultipartFile file,
                           @RequestParam long user_id,
                           @RequestParam String destination) {
        User user = userRepository.findById(user_id).orElse(null);
        Path workingDirectory = Path.of(user.getWorkingDirectory());
        String fullDestination = null;
        FileModel fileModel = null;

        // Добавляет рабочую директорию пользователя к пути файла.
        // Так файл будет сохранен в папку пользователя, а не в корень программы.
        fullDestination = workingDirectory.resolve(destination)
                .normalize()
                .toString();

        if (!destination.isEmpty()) {

            fileModel = buildFileModel(file, user_id, fullDestination);
            // Папу также сохраняем в базу данных как файл
            fetchFolderAndSave(user_id, fullDestination);
        }

        fileModelRepository.save(fileModel);
        fileSystemService.saveFile(file, fullDestination);
    }

    @PostMapping(apiName + "/uploadMultiple")
    @ResponseBody
    public void uploadFiles(@RequestParam MultipartFile[] files,
                           @RequestParam long user_id,
                           @RequestParam String destination) {
        User user = userRepository.findById(user_id).orElse(null);
        Path workingDirectory = Path.of(user.getWorkingDirectory());
        String fullDestination = null;

        // Добавляет рабочую директорию пользователя к пути файла.
        // Так файл будет сохранен в папку пользователя, а не в корень программы.
        fullDestination = workingDirectory.resolve(destination)
                .normalize()
                .toString();

        if (!destination.isEmpty()) {
            // Папу также сохраняем в базу данных как файл
            fetchFolderAndSave(user_id, fullDestination);
        }

        List<FileModel> fileModels = new ArrayList<FileModel>();
        for (MultipartFile file: files) {
            FileModel fileModel = buildFileModel(file, user_id, fullDestination);
            fileModels.add(fileModel);
        }


        fileModelRepository.saveAll(fileModels);
        fileSystemService.saveAllFiles(files, fullDestination);
    }

    // The colon is just a separator. It separates
    // path variable name from regular expression
    @GetMapping(apiName + "/download")
    @ResponseBody
    public ResponseEntity downloadFile(@RequestParam String fileName) {
        Resource resource = null;

        try {
            resource = fileSystemService.getFile(fileName);
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
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    @GetMapping(apiName + "/downloadMultiple")
    @ResponseBody
    public ResponseEntity downloadFiles(@RequestParam long userId, String[] fileNames) {
        User user = userRepository.findById(userId).orElse(null);
        String saveTo = Path.of(user.getWorkingDirectory())
                .resolve(tmpDirectory)
                .normalize()
                .toString();

        try {
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = fileSystemService.getResolvedPath(fileNames[i]).toString();
            }
            File zipFile = zipArchiverService.zip(fileNames, saveTo);
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
        User user = userRepository.findById(userId).orElse(null);

        if (user != null)
            return getFiles(user.getWorkingDirectory(), userId);

        return ResponseEntity.status(HttpStatus.CONFLICT).build();
    }

    @GetMapping(apiName + "/get")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getFiles(@RequestParam String directory, long userId) {
        FileModel probe = new FileModel();
        probe.setUserId(userId);
        probe.setPath(directory);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "size")
                .withMatcher("user_id", exact())
                .withMatcher("path", exact());

        List<FileModel> files = fileModelRepository.findAll(Example.of(probe, matcher));
        return ResponseEntity.ok().body(files);
    }

    @DeleteMapping(apiName + "/delete")
    @ResponseBody
    public void deleteFile(@RequestParam String fileName) {
        try {
            fileSystemService.deleteFile(fileName);
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
    private String getPathToFolder(String fullDestination, String defaultPath)
    {
        String pathToFolder = defaultPath.toString();
        int endIndex = fullDestination.lastIndexOf("/");
        if (endIndex > 0)
            pathToFolder = fullDestination.substring(0, endIndex);
        return pathToFolder;
    }
    private void fetchFolderAndSave(long user_id, String fullDestination)
    {
        String directoryName = StringUtils.getFilename(fullDestination);

        // Определяем путь по умолчанию
        String defaultPath;
        int firstSlashIndex = fullDestination.indexOf("/");

        // Если путь содержит несколько папок ...
        if (firstSlashIndex > 0)
            // ... выбираем корень
            defaultPath = fullDestination.substring(0, firstSlashIndex);
        else
            defaultPath = fullDestination;

        String pathToFolder = getPathToFolder(fullDestination, defaultPath);
        FileModel folder = buildFileModelFromDirectory(directoryName, user_id, pathToFolder);
        fileModelRepository.save(folder);
    }

    private FileModel buildFileModel(MultipartFile file, long user_id, String path) {
        FileModel fileModel = new FileModel();
        fileModel.setName(file.getOriginalFilename());
        fileModel.setPath(path);
        fileModel.setDateCreated(new Date());
        fileModel.setUserId(user_id);
        fileModel.setSize(file.getSize());
        fileModel.setType(FileTypes.OTHER);

        return fileModel;
    }

    private FileModel buildFileModelFromDirectory(String directoryName, long user_id, String path)
    {
        FileModel fileModel = new FileModel();
        fileModel.setName(directoryName);
        fileModel.setPath(path);
        fileModel.setDateCreated(new Date());
        fileModel.setUserId(user_id);
        fileModel.setType(FileTypes.DIRECTORY);

        return fileModel;
    }
}
