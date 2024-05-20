package com.app.controllers;

import com.app.interfaces.IFileSystemService;
import com.app.interfaces.IUserService;
import com.app.model.User;
import com.app.repositories.IUserRepository;
import org.apache.coyote.Response;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.net.http.HttpResponse;
import java.util.List;

/**
 * Предоставляет методы работы с пользователями
 */
@Controller
@CrossOrigin("http://localhost:3000")
public class UserController {
    @Autowired
    public UserController(IUserRepository userRepository,
                          IFileSystemService fileSystemService,
                          IUserService userService,
                          PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.fileSystemService = fileSystemService;
        this.userService = userService;
        this.passwordEncoder = passwordEncoder;
    }

    /**
     * Создает указаного пользователя
     * @param user
     *      Создаваемый пользователь
     */
    @PostMapping(apiName + "/create")
    @ResponseBody
    public ResponseEntity<String> createUser(@RequestBody User user) {
        if (user == null)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                            .body("Пользовател не был задан");

        if (userService.isLoginDuplicate(user.getLogin()))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Указанный логин уже существует");

        if (userService.isEmailDuplicate(user.getEmail()))
            return ResponseEntity.status(HttpStatus.CONFLICT)
                            .body("Указанный email уже существует");

        user.setWorkingDirectory(user.getLogin());
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);

        // Создание для пользователя рабочей директории, корзины и папки для временных файлов
        userService.initializeDirectories(user.getLogin());

        return ResponseEntity.ok().build();
    }

    /**
     * Получает пользователя по его id
     * @param id
     *      Id пользователя
     */
    @GetMapping(apiName + "/getById")
    @ResponseBody
    public ResponseEntity<User> getUserById(@RequestParam long id) {
        User user = userRepository.findById(id).orElse(null);
        return ResponseEntity.ok().body(user);
    }

    private final String apiName = "/user";
    private final IUserRepository userRepository;
    private final IFileSystemService fileSystemService;
    private final IUserService userService;
    private final PasswordEncoder passwordEncoder;
}
