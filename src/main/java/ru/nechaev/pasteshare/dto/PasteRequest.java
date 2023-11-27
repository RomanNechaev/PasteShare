package ru.nechaev.pasteshare.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Data;
import ru.nechaev.pasteshare.util.Marker;

@Data
public class PasteRequest {
    @NotBlank(groups = Marker.OnCreate.class)
    private String title;
    @Pattern(regexp = "^\\d{4}-\\d{2}-\\d{2}$")
    @JsonProperty("expired_at")
    private String expiredAt;
    @NotBlank(groups = Marker.OnCreate.class)
    @Pattern(regexp = "^(PUBLIC|PRIVATE)$")
    private String visibility;
    @NotBlank(groups = Marker.OnCreate.class)
    private String text;
    @NotBlank(groups = Marker.OnUpdate.class)
    @JsonProperty("public_paste_url")
    private String publicPasteUrl;
}
