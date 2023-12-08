package com.app.controllers;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import com.app.model.FileModel;
import com.app.model.User;
import com.app.repositories.IFileModelRepository;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.nio.file.Path;
import java.util.List;

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
    public ResponseEntity<List<FileModel>> getFiles() {
        return null;
    }

    // TODO: 12/8/23 Put to USER'S .trashbin directory
    @PostMapping(apiName + "/put")
    @ResponseBody
    public void putFile(@RequestParam long userId, String fileName, String filePath) {
        User user = userRepository.findById(userId).orElse(null);

        // Поиск файла по его пути и имени
        FileModel file = fileModelRepository.findByNameAndPath(fileName, filePath).orElse(null);

        String source = filePath + "/" + fileName;
        String destination = user.getWorkingDirectory() + "/" + trashbinPath + "/" + fileName;

        file.setPath(trashbinPath);

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
