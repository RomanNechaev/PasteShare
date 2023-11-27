package ru.nechaev.pasteshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;
import ru.nechaev.pasteshare.util.Marker;

@Data
public class AuthenticationRequest {
    @NotBlank(groups = Marker.OnCreate.class)
    @Size(max = 30, message = "Name length must be at most 20 characters")
    private String username;
    @NotBlank(groups = Marker.OnCreate.class)
    private String password;
    @Email
    @Size(max = 50, message = "Name length must be at most 50 characters")
    @NotBlank(groups = Marker.OnCreate.class)
    private String email;
}
