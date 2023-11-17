package ru.nechaev.pasteshare.controller;

import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nechaev.pasteshare.dto.PasteDto;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.service.PasteService;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1")
public class PasteController {
    private final PasteService pasteService;

    @PostMapping
    public ResponseEntity<PasteDto> create(@RequestBody PasteDto pasteDto) {
        Paste paste = pasteService.create(pasteDto);
        return ResponseEntity.ok().build();
    }
}
