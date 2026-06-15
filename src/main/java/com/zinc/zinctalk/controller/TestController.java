package com.zinc.zinctalk.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.zinc.zinctalk.common.Result;

@RestController
@RequestMapping("/api")
public class TestController {

    @GetMapping("/hello")
    public Result<String> hello() {
        return Result.success("hello");
    }
}