package com.app.controllers;

import com.app.interfaces.IFileSystemService;
import com.app.model.FileModel;
import com.app.model.FileTypes;
import com.app.model.User;
import com.app.repositories.IFileModelRepository;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.net.URLConnection;
import java.nio.file.Path;
import java.util.Date;

@Controller
public class FileManagementController {
    @Autowired
    public FileManagementController(IFileSystemService fileSystemService,
                                    IFileModelRepository fileModelRepository,
                                    IUserRepository userRepository) {
        this.fileSystemService = fileSystemService;
        this.fileModelRepository = fileModelRepository;
        this.userRepository = userRepository;
    }

    @PostMapping("/upload")
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

        FileModel fileModel = new FileModel();
        fileModel.setName(file.getOriginalFilename());
        fileModel.setPath(destination);
        fileModel.setDateCreated(new Date());
        fileModel.setUser_id(user_id);
        fileModel.setSize(file.getSize());
        fileModel.setType(FileTypes.OTHER);

        fileModelRepository.save(fileModel);
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
    private final IFileModelRepository fileModelRepository;
    private final IUserRepository userRepository;
}
