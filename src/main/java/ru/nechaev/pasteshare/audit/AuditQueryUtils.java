package ru.nechaev.pasteshare.audit;

import org.hibernate.envers.query.AuditQuery;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class AuditQueryUtils {
    public static <T> List<AuditQueryResult<T>> getAuditQueryResults(AuditQuery query, Class<T> targetType) {

        List<?> results = query.getResultList();

        if (results == null) {
            return new ArrayList<>();
        }
        return results.stream()
                .filter(x -> x instanceof Object[])
                .map(x -> (Object[]) x)
                .map(x -> AuditQueryResultUtils.getAuditQueryResult(x, targetType))
                .collect(Collectors.toList());
    }
}
