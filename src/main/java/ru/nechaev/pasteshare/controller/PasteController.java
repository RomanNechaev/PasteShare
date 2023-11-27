package ru.nechaev.pasteshare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nechaev.pasteshare.dto.PasteHistoryResponse;
import ru.nechaev.pasteshare.dto.PasteRequest;
import ru.nechaev.pasteshare.dto.PasteResponse;
import ru.nechaev.pasteshare.entitity.Paste;
import ru.nechaev.pasteshare.entitity.PasteHistory;
import ru.nechaev.pasteshare.mappers.PasteHistoryMapper;
import ru.nechaev.pasteshare.mappers.PasteMapper;
import ru.nechaev.pasteshare.service.PasteService;

import java.util.List;
import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/paste")
@Tag(name = "Paste", description = "endpoints for paste entity")
public class PasteController {
    private final PasteService pasteService;
    private final PasteMapper pasteMapper;
    private final PasteHistoryMapper pasteHistoryMapper;

    @Operation(summary = "Create a new paste",
            description = "Note: when creating a paste, permission is also created",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "paste create successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<Void> create(@RequestBody PasteRequest pasteRequest) {
        pasteService.create(pasteRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "update an existing paste",
            description = "Note: when updating a paste, the previous revision is saved",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "paste update successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    )
            }
    )
    @PutMapping
    public ResponseEntity<Void> update(@RequestBody PasteRequest pasteRequest) {
        pasteService.update(pasteRequest);
        return ResponseEntity.ok().build();
    }

    @Operation(summary = "find paste by publicId",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "paste get successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "permission denied"
                    )
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "publicId",
                            schema = @Schema(
                                    type = "string",
                                    format = "string",
                                    description = "publicId used like a url"
                            )
                    )
            }
    )
    @GetMapping({"{publicId}"})
    public ResponseEntity<PasteResponse> getPasteByPublicUrl(@PathVariable String publicId) {
        Paste paste = pasteService.getPasteByPublicId(publicId);
        PasteResponse response = getPasteResponse(paste);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "find paste by uuid and version",
            responses = {
                    @ApiResponse(
                            content = @Content(mediaType = "application.json"),
                            responseCode = "200",
                            description = "paste get successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "permission denied"
                    )
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "uuid",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    description = "the generated UUID"
                            )
                    ),
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "version",
                            schema = @Schema(
                                    type = "number",
                                    format = "long",
                                    description = "the version of paste"
                            )
                    )
            }
    )
    @GetMapping({"/{uuid}/{version}"})
    public ResponseEntity<PasteResponse> getPasteByVersion(@PathVariable UUID uuid, @PathVariable Long version) {
        Paste paste = pasteService.getPasteByVersion(uuid, version);
        PasteResponse response = getPasteResponse(paste);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "find paste by publicId and version",
            responses = {
                    @ApiResponse(
                            content = @Content(mediaType = "application.json"),
                            responseCode = "200",
                            description = "paste get successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "permission denied"
                    )
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "publicId",
                            schema = @Schema(
                                    type = "string",
                                    format = "string",
                                    description = "publicId used like a url"
                            )
                    ),
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "version",
                            schema = @Schema(
                                    type = "number",
                                    format = "long",
                                    description = "the version of paste"
                            )
                    )
            }
    )
    @GetMapping("/public/{publicId}/{version}")
    public ResponseEntity<PasteResponse> getPasteByVersionAndPublicId(@PathVariable String publicId, @PathVariable Long version) {
        Paste paste = pasteService.getPasteByVersionAndPublicId(publicId, version);
        PasteResponse response = getPasteResponse(paste);
        return ResponseEntity.ok(response);
    }

    @Operation(summary = "find all paste by userId",
            responses = {
                    @ApiResponse(
                            content = @Content(mediaType = "application.json"),
                            responseCode = "200",
                            description = "pastes get successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "permission denied"
                    )
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "userId",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    description = "the generated UUID"
                            )
                    )
            }
    )
    @GetMapping("/all/{userId}")
    public ResponseEntity<List<PasteResponse>> getAllPasteByUser(@PathVariable UUID userId) {
        List<Paste> pastes = pasteService.getPastesByUserId(userId);
        return ResponseEntity.ok(
                pastes.stream().map(this::getPasteResponse).toList()
        );
    }

    @Operation(summary = "find all paste revision",
            responses = {
                    @ApiResponse(
                            content = @Content(mediaType = "application.json"),
                            responseCode = "200",
                            description = "pastes get successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    ),
                    @ApiResponse(
                            responseCode = "403",
                            description = "permission denied"
                    )
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "pasteId",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    description = "the generated UUID"
                            )
                    )
            }
    )
    @GetMapping("/all/versions/{pasteId}")
    public ResponseEntity<List<PasteHistoryResponse>> getAllPasteByVersion(@PathVariable UUID pasteId) {
        List<PasteHistory> pasteHistories = pasteService.getAllPasteRevision(pasteId);
        List<PasteHistoryResponse> pasteHistoryResponses = pasteHistoryMapper.toListDto(pasteHistories);
        pasteHistoryResponses.forEach(x ->
                x.getPasteResponse().setContent(
                        pasteService.getPasteContent(x.getPasteResponse().getId())
                )
        );
        return ResponseEntity.ok(pasteHistoryResponses);
    }

    @Operation(summary = "delete a paste",
            description = "Note: when deleting a paste, the permission is also deleted",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "delete successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "paste not found")
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "uuid",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    description = "the generated UUID"
                            )
                    )
            }
    )
    @DeleteMapping("/{uuid}")
    public ResponseEntity<Void> delete(@PathVariable UUID uuid) {
        pasteService.delete(uuid);
        return ResponseEntity.noContent().build();
    }

    private PasteResponse getPasteResponse(Paste paste) {
        PasteResponse response = pasteMapper.toDto(paste);
        String content = pasteService.getPasteContent(paste.getId());
        response.setContent(content);
        return response;
    }
}
