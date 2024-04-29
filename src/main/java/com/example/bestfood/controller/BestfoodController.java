package com.example.bestfood.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class BestfoodController {

    @GetMapping("/")
    public String main() {

        return "main";
    }
}
