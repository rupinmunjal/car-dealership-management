package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.*;
import ca.sheridancollege.munjalru.services.DealerManagementService;
import ca.sheridancollege.munjalru.services.DealerService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/v1/dealers")
@RequiredArgsConstructor
public class DealerRestController {

    private final DealerService dealerService;
    private final DealerManagementService dealerManagementService;

    /**
     * Returns dealers scoped to the caller's dealer.
     * SITE_ADMIN sees all dealers; DEALER_ADMIN/EMPLOYEE see only their own.
     */
    @GetMapping
    public ResponseEntity<Page<DealerResponse>> getAllDealers(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == Role.SITE_ADMIN) {
            return ResponseEntity.ok(dealerService.findAll(pageable));
        }
        Long dealerId = currentUser.getDealer() != null
                ? currentUser.getDealer().getId()
                : null;
        return ResponseEntity.ok(dealerService.findByIdAsPage(dealerId, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<DealerResponse> getDealerById(@PathVariable Long id,
                                                        @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == Role.SITE_ADMIN) {
            return ResponseEntity.ok(dealerService.findById(id));
        }
        Long dealerId = currentUser.getDealer() != null
                ? currentUser.getDealer().getId()
                : null;
        if (!id.equals(dealerId)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealerService.findById(id));
    }

    @GetMapping("/stats/inventory")
    public ResponseEntity<Map<String, Long>> getTotalInventory() {
        return ResponseEntity.ok(Map.of("totalInventory", dealerService.countTotalInventory()));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<DealerResponse> addDealer(@Valid @RequestBody DealerRequest dealerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealerService.create(dealerRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<DealerResponse> updateDealer(@PathVariable Long id,
                                                       @Valid @RequestBody DealerRequest dealerRequest,
                                                       @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN
                && (currentUser.getDealer() == null
                || !id.equals(currentUser.getDealer().getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealerService.update(id, dealerRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id) {
        dealerService.delete(id);
        return ResponseEntity.ok().build();
    }

    // ── Phase 2: Dealer account creation (SITE_ADMIN only) ──────────

    /**
     * Creates a new Dealer and its initial DEALER_ADMIN in one operation.
     */
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<DealerResponse> registerDealer(
            @Valid @RequestBody CreateDealerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dealerManagementService.createDealerWithAdmin(request));
    }

    // ── Phase 2: Dealer status (SITE_ADMIN only) ────────────────────

    /**
     * Updates a dealer's operational status (ACTIVE / SUSPENDED).
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<DealerResponse> updateDealerStatus(
            @PathVariable Long id,
            @Valid @RequestBody DealerStatusRequest request) {
        DealerStatus newStatus = DealerStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(dealerManagementService.updateStatus(id, newStatus));
    }

    // ── Phase 2: Package assignment (SITE_ADMIN only) ───────────────

    /**
     * Assigns a subscription package to a dealer.
     */
    @PutMapping("/{id}/package")
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<DealerResponse> assignPackage(
            @PathVariable Long id,
            @RequestBody Map<String, Long> body) {
        Long packageId = body.get("packageId");
        if (packageId == null) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
        return ResponseEntity.ok(dealerManagementService.assignPackage(id, packageId));
    }

    // ── Phase 2: Dealer settings (DEALER_ADMIN scoped) ──────────────

    /**
     * Updates a dealer's public-facing settings. SITE_ADMIN can modify any
     * dealer; DEALER_ADMIN can only modify their own.
     */
    @PutMapping("/{id}/settings")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<DealerResponse> updateDealerSettings(
            @PathVariable Long id,
            @Valid @RequestBody DealerSettingsRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN) {
            if (currentUser.getDealer() == null || !id.equals(currentUser.getDealer().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (currentUser.getDealerStatus() == DealerStatus.SUSPENDED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(dealerManagementService.updateSettings(id, request));
    }
}
