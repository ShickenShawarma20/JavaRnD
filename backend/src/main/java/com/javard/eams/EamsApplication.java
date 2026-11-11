package com.javard.eams;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class EamsApplication {

    public static void main(String[] args) {
        SpringApplication.run(EamsApplication.class, args);
    }
}

