package com.pawsitters.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * Startseite der Pawsitters-Anwendung.
 */
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "index";
    }
}
