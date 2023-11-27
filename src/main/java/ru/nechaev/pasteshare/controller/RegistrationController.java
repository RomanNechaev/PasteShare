package ru.nechaev.pasteshare.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nechaev.pasteshare.dto.AuthenticationRequest;
import ru.nechaev.pasteshare.service.UserService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class RegistrationController {
    private final UserService userService;

    @Operation(summary = "registration a new user",
            responses = {
                    @ApiResponse(
                            responseCode = "409",
                            description = "user already exists!"
                    ),
                    @ApiResponse(
                            responseCode = "201",
                            description = "user create successfully"),
            },
            description = "Note: the csrf token for registration and" +
                    "the token for api requests with a verified cookie are different"
    )
    @PostMapping("/registration")
    public ResponseEntity<Void> register(@RequestBody AuthenticationRequest authenticationRequest) {
        userService.create(authenticationRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }
}
