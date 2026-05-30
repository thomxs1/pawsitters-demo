package com.pawsitters.controller;

import com.pawsitters.service.UserService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class AuthController {

    private final UserService userService;

    public AuthController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "auth/login";
    }

    @GetMapping("/register")
    public String registerPage() {
        return "auth/register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String username,
            @RequestParam String email,
            @RequestParam String password,
            @RequestParam String displayName,
            @RequestParam(required = false, defaultValue = "false") boolean becomeOwner,
            @RequestParam(required = false, defaultValue = "false") boolean becomeHost,
            Model model
    ) {
        try {
            userService.register(new UserService.RegisterRequest(
                username.trim(),
                email.trim(),
                password,
                displayName.trim(),
                becomeOwner,
                becomeHost
            ));
            return "redirect:/login?registered";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("error", ex.getMessage());
            model.addAttribute("username", username);
            model.addAttribute("email", email);
            model.addAttribute("displayName", displayName);
            model.addAttribute("becomeOwner", becomeOwner);
            model.addAttribute("becomeHost", becomeHost);
            return "auth/register";
        }
    }
}
