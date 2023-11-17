package ru.nechaev.pasteshare.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import ru.nechaev.pasteshare.util.Marker;

@Data
public class PasteDto {
    private String title;
    private String expiredAt;
    @NotBlank(groups = Marker.OnCreate.class)
    private String visibility;
    @NotBlank(groups = Marker.OnCreate.class)
    private String text;
}
