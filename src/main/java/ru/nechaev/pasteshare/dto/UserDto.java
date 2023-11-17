package ru.nechaev.pasteshare.dto;

import jakarta.validation.constraints.Email;
import lombok.Data;

@Data
public class UserDto {
    private String username;
    private String password;
    @Email
    private String email;
}
