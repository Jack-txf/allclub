package com.feng.circle.server.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/circle/test")
public class TestController {

    @GetMapping("/hello")
    public String testString() {
        return "Hello";
    }
}
