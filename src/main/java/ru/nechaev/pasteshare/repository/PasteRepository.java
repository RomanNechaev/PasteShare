package ru.nechaev.pasteshare.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.nechaev.pasteshare.entitity.Paste;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface PasteRepository extends JpaRepository<Paste, UUID> {
    Optional<Paste> findPasteByContentLocation(String publicPasteId);

    @Query(value = "SELECT id, user_id, title, content_location, created_at, expired_at, visibility, last_visited, version" +
            " FROM public.paste_aud" +
            " WHERE paste_aud.content_location=?1 AND paste_aud.version=?2", nativeQuery = true)
    List<Paste> getPastesByUserId(UUID userId);

    void deleteByUserId(UUID userId);

    boolean existsByContentLocation(String contentLocation);
}
