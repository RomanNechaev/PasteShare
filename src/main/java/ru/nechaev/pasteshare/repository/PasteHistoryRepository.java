package ru.nechaev.pasteshare.repository;

import ru.nechaev.pasteshare.entitity.PasteHistory;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasteHistoryRepository {
    List<PasteHistory> getPasteRevisions(UUID pasteId);

    Optional<PasteHistory> findPasteByVersion(UUID uuid, Long version);

    Optional<PasteHistory> findPasteByVersionAndPublicId(String publicId, Long version);
}
