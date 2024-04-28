package com.example.demo.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DemoController {
    @GetMapping("/") //  "localhost:8080/test" 접속 시 아래 test 함수 실행함
    public String test() {

        return "test"; // templates 의 test.html로 가라는 것.// git push test
    }
}
