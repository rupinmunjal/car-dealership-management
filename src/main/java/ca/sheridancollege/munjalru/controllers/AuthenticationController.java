package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.exception.ApiError;
import ca.sheridancollege.munjalru.models.AuthenticationRequest;
import ca.sheridancollege.munjalru.models.AuthenticationResponse;
import ca.sheridancollege.munjalru.services.AuthenticationService;
import io.micrometer.core.instrument.MeterRegistry;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirements;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.AllArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@AllArgsConstructor
@Tag(name = "Authentication", description = "Registration and login (public)")
public class AuthenticationController {
    private final AuthenticationService authenticationService;
    private final MeterRegistry meterRegistry;

    @Operation(summary = "Register a new user", description = "Creates a new user account. The first user is auto-assigned SITE_ADMIN; subsequent users get the default role.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "User registered successfully",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @SecurityRequirements
    @PostMapping("/register")
    public ResponseEntity<AuthenticationResponse> register(@Valid @RequestBody AuthenticationRequest request) {
        return ResponseEntity.ok(authenticationService.register(request));
    }

    @Operation(summary = "Login", description = "Authenticates a user and returns a JWT with role, dealerId, permissions, and dealerStatus claims.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Authentication successful",
                    content = @Content(schema = @Schema(implementation = AuthenticationResponse.class))),
            @ApiResponse(responseCode = "401", description = "Invalid email or password",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Account is deactivated",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @SecurityRequirements
    @PostMapping("/login")
    public ResponseEntity<AuthenticationResponse> authenticate(@Valid @RequestBody AuthenticationRequest request) {
        try {
            return ResponseEntity.ok(authenticationService.authenticate(request));
        } catch (AuthenticationException ex) {
            meterRegistry.counter("dealership.auth.failures").increment();
            throw ex;
        }
    }
}
