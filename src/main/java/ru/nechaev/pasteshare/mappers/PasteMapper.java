package ru.nechaev.pasteshare.mappers;

import org.springframework.stereotype.Component;
import ru.nechaev.pasteshare.dto.PasteResponse;
import ru.nechaev.pasteshare.entitity.Paste;

import java.util.List;

@Component
public class PasteMapper implements Mappable<PasteResponse, Paste> {
    @Override
    public PasteResponse toDto(Paste paste) {
        if (paste == null) {
            return null;
        }
        return new PasteResponse(
                paste.getId(),
                paste.getUser().getId(),
                paste.getTitle(),
                paste.getContentLocation(),
                paste.getCreatedAt(),
                paste.getExpiredAt(),
                paste.getVisibility(),
                paste.getLastVisited(),
                paste.getVersion(),
                null
        );
    }

    @Override
    public List<PasteResponse> toListDto(List<Paste> pasteList) {
        if (pasteList == null) return null;
        return pasteList.stream().map(this::toDto).toList();
    }
}
