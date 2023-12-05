package ru.nechaev.pasteshare.repository.impl;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.hibernate.envers.AuditReader;
import org.hibernate.envers.AuditReaderFactory;
import org.hibernate.envers.query.AuditEntity;
import org.hibernate.envers.query.AuditQuery;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.nechaev.pasteshare.audit.AuditQueryResult;
import ru.nechaev.pasteshare.audit.AuditQueryUtils;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.PasteHistory;
import ru.nechaev.pasteshare.repository.PasteHistoryRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class PasteHistoryRepositoryImpl implements PasteHistoryRepository {
    @PersistenceContext
    private EntityManager entityManager;

    public PasteHistory getPasteHistory(AuditQueryResult<Paste> auditQueryResult) {
        return new PasteHistory(
                auditQueryResult.entity(),
                auditQueryResult.type()
        );
    }

    @Override
    @Transactional(readOnly = true)
    public List<PasteHistory> getPasteRevisions(UUID pasteId) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery auditQuery = auditReader.createQuery()
                .forRevisionsOfEntity(Paste.class, false, true)
                .add(AuditEntity.id().eq(pasteId));

        return AuditQueryUtils.getAuditQueryResults(auditQuery, Paste.class).stream()
                .map(this::getPasteHistory)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<PasteHistory> findPasteByVersionAndPublicId(String publicId, Long version) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery auditQuery = auditReader.createQuery()
                .forRevisionsOfEntity(Paste.class, false, false)
                .add(AuditEntity.property("contentLocation").eq(publicId))
                .add(AuditEntity.property("version").eq(version));

        return AuditQueryUtils.getAuditQueryResults(auditQuery, Paste.class).stream().findFirst()
                .map(this::getPasteHistory);
    }

    @Transactional(readOnly = true)
    public Optional<PasteHistory> findPasteByVersion(UUID uuid, Long version) {
        AuditReader auditReader = AuditReaderFactory.get(entityManager);
        AuditQuery auditQuery = auditReader.createQuery()
                .forRevisionsOfEntity(Paste.class, false, false)
                .add(AuditEntity.id().eq(uuid))
                .add(AuditEntity.property("version").eq(version));

        return AuditQueryUtils.getAuditQueryResults(auditQuery, Paste.class).stream().findFirst()
                .map(this::getPasteHistory);
    }
}
