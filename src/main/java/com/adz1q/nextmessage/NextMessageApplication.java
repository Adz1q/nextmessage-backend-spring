package com.adz1q.nextmessage;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NextMessageApplication {

    public static void main(String[] args) {
        SpringApplication.run(NextMessageApplication.class, args);

        System.out.println("Hello Piesek!");
    }
}