package ru.nechaev.pasteshare.service;

import jakarta.validation.Valid;
import ru.nechaev.pasteshare.dto.PasteRequest;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.PasteHistory;

import java.util.List;
import java.util.UUID;

public interface PasteService {
    Paste getById(UUID uuid);

    Paste create(@Valid PasteRequest paste);

    void delete(UUID uuid);

    Paste update(@Valid PasteRequest paste);

    Paste getPasteByPublicId(String publicId);

    Paste getPasteByVersion(UUID uuid, Long version);

    String getPasteContent(Paste paste);

    Paste getPasteByVersionAndPublicId(String publicPasteId, Long version);

    List<Paste> getPastesByUserId(UUID userId);

    List<PasteHistory> getAllPasteRevision(UUID pasteId);
}
