package ru.nechaev.pasteshare.service;

import ru.nechaev.pasteshare.dto.PasteDto;
import ru.nechaev.pasteshare.entitity.Paste;

import java.util.UUID;

public interface PasteService {
    Paste getById(UUID uuid);

    Paste create(PasteDto paste);

    void delete(UUID uuid);

    Paste update(PasteDto paste);
}
