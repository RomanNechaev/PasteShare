package ru.nechaev.pasteshare.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Data;
import ru.nechaev.pasteshare.util.Marker;

@Data
@Builder
public class AuthenticationRequest {
    @NotBlank(groups = Marker.OnCreate.class)
    @Size(max = 30, message = "Name length must be at most 20 characters", groups = Marker.OnCreate.class)
    private String username;
    @NotBlank(groups = Marker.OnCreate.class)
    private String password;
    @Email(groups = Marker.OnCreate.class)
    @Size(max = 50, message = "Email length must be at most 50 characters", groups = Marker.OnCreate.class)
    @NotBlank(groups = Marker.OnCreate.class)
    private String email;
}
