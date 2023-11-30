package com.app.controllers;

import com.app.model.User;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class UserController {

    @Autowired
    public UserController(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/user/create")
    @ResponseBody
    public void createUser(User user) {
        userRepository.save(user);
    }

    private final IUserRepository userRepository;
}
