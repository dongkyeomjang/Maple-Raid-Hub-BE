package com.mapleraid;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MapleRaidApplication {
    public static void main(String[] args) {
        SpringApplication.run(MapleRaidApplication.class, args);
    }
}
