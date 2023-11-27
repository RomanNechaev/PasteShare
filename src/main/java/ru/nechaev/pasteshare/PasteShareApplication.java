package ru.nechaev.pasteshare;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.nechaev.pasteshare.config.S3ConfigurationProperties;

@SpringBootApplication
@EnableConfigurationProperties(S3ConfigurationProperties.class)
public class PasteShareApplication {
    public static void main(String[] args) {
        SpringApplication.run(PasteShareApplication.class, args);
    }
}
