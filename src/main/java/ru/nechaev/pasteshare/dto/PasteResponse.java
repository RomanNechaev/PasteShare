package ru.nechaev.pasteshare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import ru.nechaev.pasteshare.entitity.Visibility;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PasteResponse {
    private final UUID id;
    private final UUID user_id;
    private final String title;
    private final String publicId;
    private final LocalDateTime createdAt;
    private final LocalDateTime expiredAt;
    private final Visibility visibility;
    private final LocalDateTime lastVisited;
    private final Long version;
    private String content;
}
