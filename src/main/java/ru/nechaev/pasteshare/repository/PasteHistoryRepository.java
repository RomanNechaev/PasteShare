package ru.nechaev.pasteshare.repository;

import ru.nechaev.pasteshare.entitity.PasteHistory;

import java.util.List;
import java.util.UUID;

public interface PasteHistoryRepository {
    List<PasteHistory> getPasteRevisions(UUID pasteId);
}
