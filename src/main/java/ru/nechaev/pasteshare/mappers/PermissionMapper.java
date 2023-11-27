package ru.nechaev.pasteshare.mappers;

import org.springframework.stereotype.Component;
import ru.nechaev.pasteshare.dto.PermissionResponse;
import ru.nechaev.pasteshare.entitity.Permission;

import java.util.List;

@Component
public class PermissionMapper implements Mappable<PermissionResponse, Permission> {
    @Override
    public PermissionResponse toDto(Permission entity) {
        if (entity == null) {
            return null;
        }
        return new PermissionResponse(
                entity.getId(),
                entity.getId(),
                entity.getPaste().getId(),
                entity.getCreatedAt()
        );
    }

    @Override
    public List<PermissionResponse> toListDto(List<Permission> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).toList();
    }
}
