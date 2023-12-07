package ru.nechaev.pasteshare.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "aws")
@Getter
@Setter
public class S3ConfigurationProperties {
    private String region;
    private String endpoint;
    private String bucketName;
}
