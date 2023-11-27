package ru.nechaev.pasteshare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nechaev.pasteshare.service.UserService;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/user")
@Tag(name = "User", description = "endpoints for paste entity")
public class UserController {
    private final UserService userService;

    @Operation(summary = "delete a user",
            description = "Note: when deleting a user, the permission is also deleted",
            responses = {
                    @ApiResponse(
                            responseCode = "204",
                            description = "delete successfully"
                    ),
                    @ApiResponse(
                            responseCode = "404",
                            description = "user not found")
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
    @DeleteMapping("{userId}")
    public ResponseEntity<Void> delete(@PathVariable UUID userId) {
        userService.delete(userId);
        return ResponseEntity.noContent().build();
    }
}
