package com.yourname.finguard.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class PlaygroundController {

    @GetMapping({"/playground", "/playground/"})
    public String playground() {
        return "redirect:/app/login.html";
    }
}
