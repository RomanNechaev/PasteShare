package ru.nechaev.pasteshare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@AllArgsConstructor
public class PermissionResponse {
    private UUID id;
    private UUID userId;
    private UUID pasteId;
    private LocalDateTime createdAt;
}
