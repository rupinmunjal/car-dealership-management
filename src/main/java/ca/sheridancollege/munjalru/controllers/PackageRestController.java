package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.dto.PackageRequest;
import ca.sheridancollege.munjalru.dto.PackageResponse;
import ca.sheridancollege.munjalru.exception.ApiError;
import ca.sheridancollege.munjalru.services.PackageService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/packages")
@RequiredArgsConstructor
@PreAuthorize("hasAuthority('SITE_ADMIN')")
@Tag(name = "Packages", description = "Subscription package CRUD — SITE_ADMIN only")
@SecurityRequirement(name = "bearerAuth")
public class PackageRestController {

    private final PackageService packageService;

    @Operation(summary = "List all packages", description = "Returns all subscription packages. SITE_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of packages"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        return ResponseEntity.ok(packageService.findAll());
    }

    @Operation(summary = "Get package by ID", description = "Returns a single subscription package. SITE_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package found",
                    content = @Content(schema = @Schema(implementation = PackageResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Package not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(packageService.findById(id));
    }

    @Operation(summary = "Create a package", description = "Creates a new subscription package with seat and listing limits. SITE_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Package created",
                    content = @Content(schema = @Schema(implementation = PackageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody PackageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(packageService.create(request));
    }

    @Operation(summary = "Update a package", description = "Updates an existing package. SITE_ADMIN only. Returns 409 if updating limits would violate existing dealer constraints.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package updated",
                    content = @Content(schema = @Schema(implementation = PackageResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Package not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Update would violate existing dealer constraints",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}")
    public ResponseEntity<PackageResponse> updatePackage(@PathVariable Long id,
                                                         @Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageService.update(id, request));
    }

    @Operation(summary = "Delete a package", description = "Deletes a package. SITE_ADMIN only. Returns 409 if any dealer is currently assigned to this package.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Package not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Package still assigned to dealers",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.delete(id);
        return ResponseEntity.ok().build();
    }
}
