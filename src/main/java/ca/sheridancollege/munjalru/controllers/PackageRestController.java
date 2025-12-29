package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.dto.PackageRequest;
import ca.sheridancollege.munjalru.dto.PackageResponse;
import ca.sheridancollege.munjalru.services.PackageService;
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
public class PackageRestController {

    private final PackageService packageService;

    @GetMapping
    public ResponseEntity<List<PackageResponse>> getAllPackages() {
        return ResponseEntity.ok(packageService.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<PackageResponse> getPackageById(@PathVariable Long id) {
        return ResponseEntity.ok(packageService.findById(id));
    }

    @PostMapping
    public ResponseEntity<PackageResponse> createPackage(@Valid @RequestBody PackageRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(packageService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<PackageResponse> updatePackage(@PathVariable Long id,
                                                         @Valid @RequestBody PackageRequest request) {
        return ResponseEntity.ok(packageService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePackage(@PathVariable Long id) {
        packageService.delete(id);
        return ResponseEntity.ok().build();
    }
}
