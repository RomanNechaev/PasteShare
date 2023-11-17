package ru.nechaev.pasteshare.service.impl;

import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nechaev.pasteshare.config.S3ConfigurationProperties;
import ru.nechaev.pasteshare.dto.PasteDto;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.User;
import ru.nechaev.pasteshare.entitity.Visibility;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.service.PasteService;
import ru.nechaev.pasteshare.service.PermissionService;
import ru.nechaev.pasteshare.service.S3Service;
import ru.nechaev.pasteshare.service.UserService;
import ru.nechaev.pasteshare.util.UniqueUrlGenerator;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PasteServiceImpl implements PasteService {
    private final UserService userService;
    private final PasteRepository pasteRepository;
    private final S3Service s3Service;
    private final S3ConfigurationProperties properties;
    private final PermissionService permissionService;

    @Override
    public Paste getById(UUID uuid) {
        return null;
    }

    @Override
    public Paste create(PasteDto pasteDto) {
        String publicPasteUrl = UniqueUrlGenerator.generate();
        User user = userService.getCurrentUser();
        Paste paste = new Paste(user, pasteDto.getTitle(), publicPasteUrl, Visibility.PUBLIC, LocalDateTime.now(), 1L);
        s3Service.putObject(properties.getBucketName(), getIdForStore(publicPasteUrl, 1L), pasteDto.getText().getBytes(StandardCharsets.UTF_8));
        pasteRepository.save(paste);
        permissionService.create(user, paste);
        return paste;
    }

    private static String getIdForStore(String publicPasteUrl, Long version) {
        return publicPasteUrl + "/" + version;
    }

    @Override
    public void delete(UUID uuid) {
    }

    @Override
    public Paste update(PasteDto paste) {
        return null;
    }
}
