package ru.nechaev.pasteshare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import ru.nechaev.pasteshare.dto.PermissionRequest;
import ru.nechaev.pasteshare.dto.PermissionResponse;
import ru.nechaev.pasteshare.entitity.Permission;
import ru.nechaev.pasteshare.mappers.PermissionMapper;
import ru.nechaev.pasteshare.service.PermissionService;

import java.util.UUID;

@RestController
@AllArgsConstructor
@RequestMapping("/api/v1/permission")
@Tag(name = "Permission", description = "endpoints for paste entity")
public class PermissionController {
    private final PermissionService permissionService;
    private final PermissionMapper permissionMapper;

    @Operation(summary = "Create a new permission",
            responses = {
                    @ApiResponse(
                            responseCode = "201",
                            description = "permission create successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "user or paste not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    )
            }
    )
    @PostMapping
    public ResponseEntity<PermissionResponse> create(@RequestBody PermissionRequest permissionRequest) {
        permissionService.create(permissionRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @Operation(summary = "find permission by id",
            responses = {
                    @ApiResponse(
                            responseCode = "200",
                            description = "permission get successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "permission not found"),
                    @ApiResponse(
                            responseCode = "400",
                            description = "validation exception"
                    )
            },
            parameters = {
                    @Parameter(
                            in = ParameterIn.PATH,
                            name = "uuid",
                            schema = @Schema(
                                    type = "string",
                                    format = "uuid",
                                    description = "the generated uuid"
                            )
                    )
            }
    )
    @GetMapping("/{uuid}")
    public ResponseEntity<PermissionResponse> getPermissionById(@PathVariable UUID uuid) {
        Permission permission = permissionService.getById(uuid);
        return ResponseEntity.ok(permissionMapper.toDto(permission));
    }

    @Operation(summary = "delete a paste",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "delete successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "permission not found")
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
        permissionService.delete(uuid);
        return ResponseEntity.noContent().build();
    }
}
