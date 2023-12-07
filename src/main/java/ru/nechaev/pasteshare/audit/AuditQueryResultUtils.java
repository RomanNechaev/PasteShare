package ru.nechaev.pasteshare.audit;

import lombok.NoArgsConstructor;
import org.hibernate.envers.RevisionType;
import ru.nechaev.pasteshare.entitity.CustomRevisionEntity;

@NoArgsConstructor
public class AuditQueryResultUtils {
    public static <T> AuditQueryResult<T> getAuditQueryResult(Object[] item, Class<T> type) {
        //3 - обязательные поля для аудирования: сущность, версия, тип версии, если меньше
        if (item == null || item.length < 3) {
            return null;
        }

        T entity = null;
        if (type.isInstance(item[0])) {
            entity = type.cast(item[0]);
        }

        CustomRevisionEntity revision = null;
        if (item[1] instanceof CustomRevisionEntity) {
            revision = (CustomRevisionEntity) item[1];
        }

        RevisionType revisionType = null;
        if (item[2] instanceof RevisionType) {
            revisionType = (RevisionType) item[2];
        }

        return new AuditQueryResult<>(entity, revision, revisionType);
    }
}
