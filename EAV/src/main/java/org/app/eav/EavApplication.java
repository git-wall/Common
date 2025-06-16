package org.app.eav;

import org.app.eav.jpa.EnableAutoJPA;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@EnableAutoJPA
public class EavApplication {

    public static void main(String[] args) {
        SpringApplication.run(EavApplication.class, args);
    }

}
