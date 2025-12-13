package com.yourname.finguard.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeRedirectController {

    @GetMapping({"/", "/index", "/app", "/app/"})
    public String rootToLogin() {
        return "redirect:/app/dashboard.html";
    }
}
