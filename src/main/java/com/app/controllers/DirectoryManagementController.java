package com.app.controllers;

import com.app.interfaces.IFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class DirectoryManagementController {
    @Autowired
    public DirectoryManagementController(IFileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @PostMapping(apiName + "/create")
    @ResponseBody
    public void createDirectory(@RequestParam String directoryName) {
        try {
            fileSystemService.createDirectory(directoryName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @DeleteMapping(apiName + "/delete")
    @ResponseBody
    public void deleteDirectory(@RequestParam String directoryName) {
        try {
            fileSystemService.deleteDirectory(directoryName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final String apiName = "/directory";
    private final IFileSystemService fileSystemService;
}
