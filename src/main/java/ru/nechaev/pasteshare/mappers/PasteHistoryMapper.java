package ru.nechaev.pasteshare.mappers;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.nechaev.pasteshare.dto.PasteHistoryResponse;
import ru.nechaev.pasteshare.entitity.PasteHistory;

import java.util.List;

@Component
@RequiredArgsConstructor
public class PasteHistoryMapper implements Mappable<PasteHistoryResponse, PasteHistory> {
    private final PasteMapper pasteMapper;

    @Override
    public PasteHistoryResponse toDto(PasteHistory entity) {
        if (entity == null) {
            return null;
        }
        return new PasteHistoryResponse(pasteMapper.toDto(entity.paste()));
    }

    @Override
    public List<PasteHistoryResponse> toListDto(List<PasteHistory> entities) {
        if (entities == null) return null;
        return entities.stream().map(this::toDto).toList();
    }
}
