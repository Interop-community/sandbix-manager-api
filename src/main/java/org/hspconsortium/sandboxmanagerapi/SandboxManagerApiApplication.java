package org.hspconsortium.sandboxmanagerapi;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.ComponentScan;

@ComponentScan({"org.hspconsortium"})
@SpringBootApplication
public class SandboxManagerApiApplication {

    public static void main(String[] args) {
        SpringApplication.run(SandboxManagerApiApplication.class, args);
    }

}
