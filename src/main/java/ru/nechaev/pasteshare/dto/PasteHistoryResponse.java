package ru.nechaev.pasteshare.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class PasteHistoryResponse {
    PasteResponse pasteResponse;
}
