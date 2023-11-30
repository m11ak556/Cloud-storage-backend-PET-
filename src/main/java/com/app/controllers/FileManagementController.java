package com.app.controllers;

import com.app.interfaces.IFileSystemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;

@Controller
public class FileManagementController {
    @Autowired
    public FileManagementController(IFileSystemService fileSystemService) {
        this.fileSystemService = fileSystemService;
    }

    @PostMapping("/upload")
    @ResponseBody
    public void uploadFile(@RequestParam MultipartFile file, @RequestParam String destination) {
        fileSystemService.saveFile(file, destination);
    }

    // The colon is just a separator. It separates
    // path variable name from regular expression
    @GetMapping("/download")
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

    @GetMapping("/getFiles")
    @ResponseBody
    public void getFiles(@RequestParam String directory) {

    }

    @DeleteMapping("/delete")
    @ResponseBody
    public void deleteFile(@RequestParam String fileName) {
        try {
            fileSystemService.deleteFile(fileName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    private final IFileSystemService fileSystemService;
}
