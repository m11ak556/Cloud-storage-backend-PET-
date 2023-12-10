package com.app.controllers;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import com.app.model.FileModel;
import com.app.model.User;
import com.app.model.id.FileModelId;
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
@CrossOrigin("http://localhost:3000")
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

    @PutMapping(apiName + "/restore")
    public void restoreFile(@RequestParam long userId, @RequestParam long fileId) {
        User user = userRepository.findById(userId).orElse(null);
        Path workingDirPath = Path.of(user.getWorkingDirectory());

        FileModelId id = new FileModelId(fileId, userId);
        FileModel fileModel = fileModelRepository.findById(id).orElse(null);
        fileModel.setDeleted(false);

        String source = workingDirPath
                .resolve(trashbinPath)
                .resolve(fileModel.getName())
                .normalize()
                .toString();

        String destination = workingDirPath
                .resolve(fileModel.getPath())
                .normalize()
                .toString();

        fileModelRepository.save(fileModel);
        fileSystemService.moveFile(source, destination);
    }

    @DeleteMapping(apiName + "/destroy")
    @ResponseBody
    public void destroyFile(@RequestParam long userId, @RequestParam long fileId) {
        User user = userRepository.findById(userId).orElse(null);
        Path workingDirPath = Path.of(user.getWorkingDirectory());

        FileModelId id = new FileModelId(fileId, userId);
        FileModel fileModel = fileModelRepository.findById(id).orElse(null);

        String fullPath = workingDirPath
                .resolve(trashbinPath)
                .resolve(fileModel.getName())
                .normalize()
                .toString();

        fileModelRepository.delete(fileModel);
        fileSystemService.deleteFile(fullPath);
    }

    private final String trashbinPath;
    private final String apiName = "/trashbin";
    private final IFileSystemService fileSystemService;
    private final IFileModelRepository fileModelRepository;
    private final IUserRepository userRepository;
}
