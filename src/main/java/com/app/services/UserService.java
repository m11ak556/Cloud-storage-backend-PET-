package com.app.services;

import com.app.configuration.FileSystemConfiguration;
import com.app.interfaces.IFileSystemService;
import com.app.interfaces.IUserService;
import com.app.model.User;
import com.app.repositories.IUserRepository;
import org.springframework.data.domain.Example;
import org.springframework.data.domain.ExampleMatcher;
import org.springframework.stereotype.Service;

import static org.springframework.data.domain.ExampleMatcher.GenericPropertyMatchers.caseSensitive;

@Service
public class UserService implements IUserService {
    public UserService(IUserRepository userRepository,
                       IFileSystemService fileSystemService,
                       FileSystemConfiguration fileSystemConfiguration) {
        this.userRepository = userRepository;
        this.fileSystemService = fileSystemService;
        trasbinDirectory = fileSystemConfiguration.getTrashbinDirectory();
        tmpDirectory = fileSystemConfiguration.getTmpDirectory();
    }

    public boolean isLoginDuplicate(String login) {
        return userRepository.findUserByLogin(login).isPresent();
    }

    public boolean isEmailDuplicate(String email) {
        User probe = new User();
        probe.setEmail(email);

        ExampleMatcher matcher = ExampleMatcher.matching()
                .withIgnorePaths("id")
                .withMatcher("email", caseSensitive());

        return userRepository.exists(Example.of(probe, matcher));
    }

    public void initializeDirectories(String login) {
        fileSystemService.createDirectory(login);
        fileSystemService.createDirectory(login + "/" + trasbinDirectory);
        fileSystemService.createDirectory(login + "/" + tmpDirectory);
    }

    private final IUserRepository userRepository;
    private final IFileSystemService fileSystemService;
    private String trasbinDirectory;
    private String tmpDirectory;
}
