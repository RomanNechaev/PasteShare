package ru.nechaev.pasteshare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import ru.nechaev.pasteshare.util.Marker;

import java.util.UUID;

@Data
@AllArgsConstructor
public class PermissionRequest {
    @NotBlank(groups = Marker.OnCreate.class)
    private String username;
    @NotBlank(groups = Marker.OnCreate.class)
    private String publicPasteId;
    @NotBlank(groups = Marker.OnUpdate.class)
    private UUID id;
}
