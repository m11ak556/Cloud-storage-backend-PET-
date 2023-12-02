package com.app.controllers;

import com.app.model.User;
import com.app.repositories.IUserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Controller
public class UserController {

    @Autowired
    public UserController(IUserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @PostMapping("/user/create")
    @ResponseBody
    public void createUser(@RequestBody User user) {
        userRepository.save(user);
    }

    @GetMapping("/user/get")
    @ResponseBody
    public ResponseEntity<List<User>> getUsers() {
        List<User> users = userRepository.findAll();
        return ResponseEntity.ok().body(users);
    }

    @GetMapping("/user/getById")
    @ResponseBody
    public ResponseEntity<User> getUserById(@RequestParam long id) {
        User user = userRepository.findById(id).orElse(null);
        return ResponseEntity.ok().body(user);
    }

    private final IUserRepository userRepository;
}
