package com.app.controllers;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import com.app.model.FileModel;
import com.app.model.User;
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

@Controller
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

    @GetMapping(apiName + "/get")
    @ResponseBody
    public ResponseEntity<List<FileModel>> getFiles(@RequestParam long userId) {
        FileModel probe = new FileModel();
        probe.setUserId(userId);
        probe.setDeleted(true);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id", "size")
                .withMatcher("user_id", exact())
                .withMatcher("is_deleted", exact());

        List<FileModel> fileModels = fileModelRepository.findAll(Example.of(probe, matcher));
        return ResponseEntity.ok(fileModels);
    }

    @PostMapping(apiName + "/put")
    @ResponseBody
    public void putFile(@RequestParam long userId, String fileName, String filePath) {
        User user = userRepository.findById(userId).orElse(null);

        // Поиск файла по его пути и имени
        FileModel file = fileModelRepository.findByNameAndPath(fileName, filePath).orElse(null);

        String source = user.getWorkingDirectory() + "/" + filePath + "/" + fileName;
        String destination = user.getWorkingDirectory() + "/" + trashbinPath;

        file.setDeleted(true);

        fileModelRepository.save(file);
        fileSystemService.moveFile(source, destination);
    }

    // На фронтэнде это можно сделать через контроллеры файлов и папок
//    @DeleteMapping("/trashbin/destroy")
//    @ResponseBody
//    public void destroyFile(@RequestParam String fileName) {
//        fileSystemService.deleteForce(trashbinPath + "/" + fileName);
//    }

    private final String trashbinPath;
    private final String apiName = "/trashbin";
    private final IFileSystemService fileSystemService;
    private final IFileModelRepository fileModelRepository;
    private final IUserRepository userRepository;
}
