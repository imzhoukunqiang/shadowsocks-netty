package org.netty;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.util.TimeZone;

@SpringBootApplication
public class Application {
    public static void main(String[] args) {
        TimeZone zone = TimeZone.getTimeZone("GMT+8");
        TimeZone.setDefault(zone);
        SpringApplication.run(Application.class, args);
    }
}

