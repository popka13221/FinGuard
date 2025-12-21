package com.myname.finguard.config;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeRedirectController {

    @GetMapping({"/", "/index"})
    public String landingPage() {
        return "forward:/index.html";
    }

    @GetMapping({"/app", "/app/"})
    public String appRoot() {
        return "redirect:/app/login.html";
    }
}
