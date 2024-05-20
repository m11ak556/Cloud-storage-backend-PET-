package com.app.controllers;

import com.app.model.User;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 * Предоставляет методы аутентификации пользователя в системе
 */
@Controller
public class AuthenticationController {
    @Autowired
    public AuthenticationController(IUserRepository userRepository,
                                    PasswordEncoder passwordEncoder) {
        this.passwordEncoder = passwordEncoder;
        this.userRepository = userRepository;
    }

    @GetMapping(apiName + "/login")
    @ResponseBody
    public ResponseEntity<Long> login(@RequestParam String login, @RequestParam String password) {
        User user = userRepository.findUserByLogin(login).orElse(null);

        if (user == null)
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();

        if (!passwordEncoder.matches(password, user.getPassword()))
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();

        return ResponseEntity.ok(user.getId());
    }

    private final IUserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String apiName = "/auth";
}
