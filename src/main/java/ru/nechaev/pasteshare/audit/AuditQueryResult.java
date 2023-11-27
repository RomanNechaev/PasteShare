package ru.nechaev.pasteshare.audit;

import org.hibernate.envers.RevisionType;
import ru.nechaev.pasteshare.entitity.CustomRevisionEntity;

public record AuditQueryResult<T>(T entity, CustomRevisionEntity revision, RevisionType type) {
}
