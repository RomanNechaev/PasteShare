package ru.nechaev.pasteshare.config;

import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;

import java.net.URI;
import java.net.URISyntaxException;

@Configuration
@AllArgsConstructor
public class S3Config {
    private final S3ConfigurationProperties properties;

    @Bean
    public S3Client s3Client() throws URISyntaxException {
        return S3Client.builder()
                .endpointOverride(new URI(properties.getEndpoint()))
                .region(Region.of(properties.getRegion()))
                .build();
    }
}
