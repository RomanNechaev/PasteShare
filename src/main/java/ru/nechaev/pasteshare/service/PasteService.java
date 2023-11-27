package ru.nechaev.pasteshare.service;

import jakarta.validation.Valid;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import ru.nechaev.pasteshare.dto.PasteRequest;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.PasteHistory;

import java.util.List;
import java.util.UUID;

public interface PasteService {
    Paste getById(UUID uuid);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    Paste create(@Valid PasteRequest paste);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    void delete(UUID uuid);

    @Transactional(isolation = Isolation.REPEATABLE_READ)
    Paste update(@Valid PasteRequest paste);

    Paste getPasteByPublicId(String publicId);

    Paste getPasteByVersion(UUID uuid, Long version);

    String getPasteContent(UUID uuid);

    Paste getPasteByVersionAndPublicId(String publicPasteId, Long version);

    List<Paste> getPastesByUserId(UUID userId);

    List<PasteHistory> getAllPasteRevision(UUID pasteId);
}
