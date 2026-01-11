package com.myname.finguard.config;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
@Profile("!prod")
public class PlaygroundController {

    @GetMapping({"/playground", "/playground/"})
    public String playground() {
        return "redirect:/app/login.html";
    }
}
