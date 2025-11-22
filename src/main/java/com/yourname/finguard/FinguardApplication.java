package com.yourname.finguard;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class FinguardApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinguardApplication.class, args);
    }
}
