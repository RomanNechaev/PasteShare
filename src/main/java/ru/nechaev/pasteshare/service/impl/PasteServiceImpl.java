package ru.nechaev.pasteshare.service.impl;

import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Isolation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.validation.annotation.Validated;
import ru.nechaev.pasteshare.config.S3ConfigurationProperties;
import ru.nechaev.pasteshare.dto.PasteRequest;
import ru.nechaev.pasteshare.dto.PermissionRequest;
import ru.nechaev.pasteshare.entitity.*;
import ru.nechaev.pasteshare.exception.access.PermissionDeniedEntityAccessException;
import ru.nechaev.pasteshare.exception.db.EntityNotFoundException;
import ru.nechaev.pasteshare.repository.PasteHistoryRepository;
import ru.nechaev.pasteshare.repository.PasteRepository;
import ru.nechaev.pasteshare.repository.PermissionRepository;
import ru.nechaev.pasteshare.repository.UserRepository;
import ru.nechaev.pasteshare.service.PasteService;
import ru.nechaev.pasteshare.service.PermissionService;
import ru.nechaev.pasteshare.service.S3Service;
import ru.nechaev.pasteshare.service.UserService;
import ru.nechaev.pasteshare.util.Marker;
import ru.nechaev.pasteshare.util.UniqueUrlGenerator;
import ru.nechaev.pasteshare.util.Verifier;

import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.UUID;

@Service
@AllArgsConstructor
@Validated
public class PasteServiceImpl implements PasteService {
    private final UserService userService;
    private final PasteRepository pasteRepository;
    private final S3Service s3Service;
    private final S3ConfigurationProperties properties;
    private final PermissionService permissionService;
    private final Verifier verifier;
    private final UserRepository userRepository;
    private final PermissionRepository permissionRepository;
    private final PasteHistoryRepository pasteHistoryRepository;

    @Override
    public Paste getById(UUID uuid) {
        Paste paste = pasteRepository.findById(uuid)
                .orElseThrow(() -> new EntityNotFoundException("Paste not found"));
        checkPermission(paste);
        return paste;

    }

    @Override
    @Transactional(isolation = Isolation.READ_COMMITTED)
    @Validated({Marker.OnCreate.class})
    public Paste create(@Valid PasteRequest pasteRequest) {
        String publicPasteUrl = UniqueUrlGenerator.generate();
        User user = userService.getCurrentUser();

        Paste paste = new Paste(user,
                pasteRequest.getTitle(),
                publicPasteUrl,
                Visibility.valueOf(pasteRequest.getVisibility()),
                LocalDate.parse(pasteRequest.getExpiredAt(), DateTimeFormatter.ISO_DATE).atStartOfDay(),
                1L);

        s3Service.putObject(properties.getBucketName(),
                getIdForStore(publicPasteUrl, 1L),
                pasteRequest.getText().getBytes(StandardCharsets.UTF_8));

        pasteRepository.save(paste);
        PermissionRequest permission = new PermissionRequest(user.getName(), paste.getContentLocation(), null);
        permissionService.create(permission);
        return paste;
    }

    private static String getIdForStore(String publicPasteUrl, Long version) {
        return publicPasteUrl + "/" + version;
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    public void delete(UUID uuid) {
        if (!pasteRepository.existsById(uuid)) {
            throw new EntityNotFoundException("Paste not found");
        }
        permissionRepository.deleteAllByPasteId(uuid);
        pasteRepository.deleteById(uuid);
    }

    @Override
    @Transactional(isolation = Isolation.REPEATABLE_READ)
    @Validated({Marker.OnUpdate.class})
    public Paste update(@Valid PasteRequest pasteRequest) {
        Paste paste = pasteRepository
                .findPasteByContentLocation(pasteRequest.getPublicPasteUrl())
                .orElseThrow(() -> new EntityNotFoundException("Paste not found"));
        updatePasteFields(pasteRequest, paste);
        paste.setVersion(paste.getVersion() + 1);

        s3Service.putObject(properties.getBucketName(),
                getIdForStore(paste.getContentLocation(), paste.getVersion()),
                pasteRequest.getText().getBytes(StandardCharsets.UTF_8));
        pasteRepository.save(paste);
        return paste;

    }

    /**
     * Обновляет значения сущности на не null поля, переданные в sourse
     *
     * @param source исходный класс
     * @param target класс у которого нужно обновить поля
     */
    private void updatePasteFields(PasteRequest source, Paste target) {
        BeanUtils.copyProperties(source, target, verifier.getNullPropertyName(source));
        if (source.getExpiredAt() != null) {
            target.setExpiredAt(LocalDate.parse(
                    source.getExpiredAt(),
                    DateTimeFormatter.ISO_DATE).atStartOfDay());
        }
    }

    @Override
    public Paste getPasteByPublicId(String publicId) {
        Paste paste = pasteRepository.findPasteByContentLocation(publicId)
                .orElseThrow(() -> new EntityNotFoundException("Entity not found!"));
        checkPermission(paste);
        return paste;
    }

    @Override
    public Paste getPasteByVersion(UUID uuid, Long version) {
        if (!pasteRepository.existsById(uuid)) {
            throw new EntityNotFoundException("Entity not found!");
        }
        PasteHistory pasteHistory = pasteHistoryRepository.findPasteByVersion(uuid, version)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Entity with version %d not found", version)));
        checkPermission(pasteHistory.paste());
        return pasteHistory.paste();
    }

    private void checkPermission(Paste paste) {
        if (paste.getVisibility() == Visibility.PRIVATE && !permissionService.confirm(paste, userService.getCurrentUser())) {
            throw new PermissionDeniedEntityAccessException("You dont have access to view this paste");
        }
    }

    public Paste getPasteByVersionAndPublicId(String publicPasteId, Long version) {
        if (!pasteRepository.existsByContentLocation(publicPasteId)) {
            throw new EntityNotFoundException("Entity not found!");
        }
        PasteHistory pasteHistory = pasteHistoryRepository.findPasteByVersionAndPublicId(publicPasteId, version)
                .orElseThrow(() -> new EntityNotFoundException(String.format("Entity with version %d not found", version)));
        checkPermission(pasteHistory.paste());
        return pasteHistory.paste();
    }

    @Override
    public List<Paste> getPastesByUserId(UUID userId) {
        if (!userRepository.existsById(userId)) {
            throw new EntityNotFoundException("User not found!");
        }
        User currentUser = userService.getCurrentUser();
        if (currentUser.getRole() == Role.USER && userId != currentUser.getId()) {
            throw new PermissionDeniedEntityAccessException("You dont have access to view this pastes");
        }
        return pasteRepository.getPastesByUserId(userId);
    }

    @Override
    public List<PasteHistory> getAllPasteRevision(UUID pasteId) {
        Paste paste = pasteRepository.findById(pasteId)
                .orElseThrow(() -> new EntityNotFoundException("Paste not found"));
        checkPermission(paste);
        return pasteHistoryRepository.getPasteRevisions(pasteId);
    }

    @Override
    public String getPasteContent(Paste paste) {
        return new String(s3Service.getObject(
                properties.getBucketName(),
                getIdForStore(paste.getContentLocation(), paste.getVersion())), StandardCharsets.UTF_8);
    }
}
