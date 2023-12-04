package com.app.controllers;

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
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.net.URLConnection;
import java.nio.file.Path;
import java.util.*;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.exact;

@Controller
public class FileManagementController {
    @Autowired
    public FileManagementController(IFileSystemService fileSystemService,
                                    IFileModelRepository fileModelRepository,
                                    IUserRepository userRepository,
                                    IZipArchiverService zipArchiverService) {
        this.fileSystemService = fileSystemService;
        this.fileModelRepository = fileModelRepository;
        this.userRepository = userRepository;
        this.zipArchiverService = zipArchiverService;
    }

    @PostMapping(apiName + "/upload")
    @ResponseBody
    public void uploadFile(@RequestParam MultipartFile file,
                           @RequestParam long user_id,
                           @RequestParam String destination) {
        User user = userRepository.findById(user_id).orElse(null);
        Path workingDirectory = Path.of(user.getWorkingDirectory());

        // Добавляет рабочую директорию пользователя к пути файла.
        // Так файл будет сохранен в папку пользователя, а не в корень программы.
        destination = workingDirectory.resolve(destination)
                .normalize()
                .toString();

        FileModel fileModel = buildFileModel(file, user_id, destination);

        fileModelRepository.save(fileModel);
        fileSystemService.saveFile(file, destination);
    }

    @PostMapping(apiName + "/uploadMultiple")
    @ResponseBody
    public void uploadFiles(@RequestParam MultipartFile[] files,
                           @RequestParam long user_id,
                           @RequestParam String destination) {
        User user = userRepository.findById(user_id).orElse(null);
        Path workingDirectory = Path.of(user.getWorkingDirectory());

        // Добавляет рабочую директорию пользователя к пути файла.
        // Так файл будет сохранен в папку пользователя, а не в корень программы.
        destination = workingDirectory.resolve(destination)
                .normalize()
                .toString();

        List<FileModel> fileModels = new ArrayList<FileModel>();
        for (MultipartFile file: files) {
            FileModel fileModel = buildFileModel(file, user_id, destination);
            fileModels.add(fileModel);
        }

        fileModelRepository.saveAll(fileModels);
        fileSystemService.saveAllFiles(files, destination);
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
    public ResponseEntity downloadFiles(@RequestParam String[] fileNames) {
        try {
            for (int i = 0; i < fileNames.length; i++) {
                fileNames[i] = fileSystemService.getResolvedPath(fileNames[i]).toString();
            }
            File zipFile = zipArchiverService.zip(fileNames);
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

    private FileModel buildFileModel(MultipartFile file, long user_id, String destination) {
        FileModel fileModel = new FileModel();
        fileModel.setName(file.getOriginalFilename());
        fileModel.setPath(destination);
        fileModel.setDateCreated(new Date());
        fileModel.setUserId(user_id);
        fileModel.setSize(file.getSize());
        fileModel.setType(FileTypes.OTHER);

        return fileModel;
    }
}
