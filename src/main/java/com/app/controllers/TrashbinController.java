package com.app.controllers;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.nio.file.Path;

@Controller
public class TrashbinController {
    @Autowired
    public TrashbinController(FileSystemConfiguration fileSystemConfiguration,
                              IFileSystemService fileSystemService) {
        trashbinPath = fileSystemConfiguration.getTrashbinDirectory();
        this.fileSystemService = fileSystemService;
    }

    @PostMapping("/trashbin/put")
    @ResponseBody
    public void putFile(@RequestParam String fileLocation) {

        String fileName = Path.of(fileLocation).getFileName().toString();
        fileSystemService.moveFile(fileLocation, trashbinPath + "/" + fileName);
    }

    // На фронтэнде это можно сделать через контроллеры файлов и папок
//    @DeleteMapping("/trashbin/destroy")
//    @ResponseBody
//    public void destroyFile(@RequestParam String fileName) {
//        fileSystemService.deleteForce(trashbinPath + "/" + fileName);
//    }

    private final String trashbinPath;
    private final IFileSystemService fileSystemService;
}
