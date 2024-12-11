package org.adz1q.nextmessagebackend;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class NextMessageBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(NextMessageBackendApplication.class, args);

        System.out.println("Hello Piesek!");
    }
}