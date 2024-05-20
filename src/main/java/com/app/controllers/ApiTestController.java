package com.app.controllers;

import com.app.interfaces.IFileModelService;
import com.app.model.FileTypes;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.http.HttpResponse;

/**
 * Контроллер для проведения тестирования различных методов
 */
@Controller
public class ApiTestController {
    @GetMapping("/greetings")
    @ResponseBody
    public String greetings()
    {
        String greetingsText = "Hello username, you are using my api rn ^^";
        return greetingsText;
    }

    @GetMapping("/fileType")
    @ResponseBody
    public ResponseEntity<FileTypes> guessFileType(@RequestParam String fileName) {
        return  ResponseEntity.ok(fileModelService.guessFileType(fileName));
    }

    @Autowired
    private IFileModelService fileModelService;
}
