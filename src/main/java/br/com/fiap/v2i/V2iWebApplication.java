package br.com.fiap.v2i;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class V2iWebApplication {

    public static void main(String[] args) {
        SpringApplication.run(V2iWebApplication.class, args);
    }

}
