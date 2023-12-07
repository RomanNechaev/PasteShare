package ru.nechaev.pasteshare.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;

@OpenAPIDefinition(
        info = @Info(
                contact = @Contact(
                        name = "Roman Nechaev",
                        email = "romenkagg@gmail.com"
                ),
                description = "OpenApi documentation for PasteShare. PasteShare is service for storing text blocks and share them with other users",
                title = "Swagger PasteShare",
                version = "0.9.0"
        )
)
public class OpenApiConfig {
}
