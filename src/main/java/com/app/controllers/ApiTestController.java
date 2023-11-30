package com.app.controllers;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import java.net.http.HttpResponse;

@Controller
public class ApiTestController {
    @GetMapping("/greetings")
    @ResponseBody
    public String greetings()
    {
        String greetingsText = "Hello username, you are using my api rn ^^";
        return greetingsText;
    }
}
