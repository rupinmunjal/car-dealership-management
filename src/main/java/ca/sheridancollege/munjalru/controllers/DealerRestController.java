package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.*;
import ca.sheridancollege.munjalru.exception.ApiError;
import ca.sheridancollege.munjalru.services.DealerManagementService;
import ca.sheridancollege.munjalru.services.DealerService;
import ca.sheridancollege.munjalru.services.DealerDashboardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
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
@Tag(name = "Dealers", description = "Dealer management, registration, status, settings, and package assignment")
@SecurityRequirement(name = "bearerAuth")
public class DealerRestController {

    private final DealerService dealerService;
    private final DealerManagementService dealerManagementService;
    private final DealerDashboardService dealerDashboardService;

    @Operation(summary = "List all dealers", description = "Returns dealers scoped to the caller. SITE_ADMIN sees all dealers; others see only their own dealer.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of dealers"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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

    @Operation(summary = "Get dealer by ID", description = "Returns a single dealer. SITE_ADMIN can access any dealer; others can only access their own. Returns 404 if not found, 403 if out of scope.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dealer found",
                    content = @Content(schema = @Schema(implementation = DealerResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Not authorized for this dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Dealer not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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

    @GetMapping("/{id}/dashboard-summary")
    public ResponseEntity<DealerDashboardSummary> getDashboardSummary(
            @PathVariable Long id,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN
                && (currentUser.getDealer() == null
                || !id.equals(currentUser.getDealer().getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(dealerDashboardService.getSummary(id));
    }

    @Operation(summary = "Total inventory count", description = "Returns the total number of cars across all dealerships. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Total inventory count"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/stats/inventory")
    public ResponseEntity<Map<String, Long>> getTotalInventory() {
        return ResponseEntity.ok(Map.of("totalInventory", dealerService.countTotalInventory()));
    }

    @Operation(summary = "Create a dealer", description = "Creates a new dealer (without an admin user). SITE_ADMIN only. Prefer POST /register for creating a dealer with its admin in one step.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dealer created",
                    content = @Content(schema = @Schema(implementation = DealerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<DealerResponse> addDealer(@Valid @RequestBody DealerRequest dealerRequest) {
        return ResponseEntity.status(HttpStatus.CREATED).body(dealerService.create(dealerRequest));
    }

    @Operation(summary = "Update a dealer", description = "Updates dealer name/location. SITE_ADMIN can update any dealer; DEALER_ADMIN can only update their own.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dealer updated",
                    content = @Content(schema = @Schema(implementation = DealerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role or wrong dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Dealer not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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

    @Operation(summary = "Delete a dealer", description = "Deletes a dealer and all associated cars/employees. SITE_ADMIN only. Returns 409 if dealer has employees.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Dealer deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Dealer not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Dealer has existing employees",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id) {
        dealerService.delete(id);
        return ResponseEntity.ok().build();
    }

    // ── Phase 2: Dealer account creation (SITE_ADMIN only) ──────────

    @Operation(summary = "Register dealer with admin", description = "Creates a new dealer and its initial DEALER_ADMIN user in one atomic operation. Returns 409 if email is already in use.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Dealer and admin created",
                    content = @Content(schema = @Schema(implementation = DealerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Admin email already in use",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping("/register")
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<DealerResponse> registerDealer(
            @Valid @RequestBody CreateDealerRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(dealerManagementService.createDealerWithAdmin(request));
    }

    // ── Phase 2: Dealer status (SITE_ADMIN only) ────────────────────

    @Operation(summary = "Update dealer status", description = "Sets a dealer's operational status to ACTIVE or SUSPENDED. Suspended dealers cannot perform any operations. SITE_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Status updated",
                    content = @Content(schema = @Schema(implementation = DealerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Invalid status value",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Dealer not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{id}/status")
    @PreAuthorize("hasAuthority('SITE_ADMIN')")
    public ResponseEntity<DealerResponse> updateDealerStatus(
            @PathVariable Long id,
            @Valid @RequestBody DealerStatusRequest request) {
        DealerStatus newStatus = DealerStatus.valueOf(request.getStatus().toUpperCase());
        return ResponseEntity.ok(dealerManagementService.updateStatus(id, newStatus));
    }

    // ── Phase 2: Package assignment (SITE_ADMIN only) ───────────────

    @Operation(summary = "Assign package to dealer", description = "Assigns a subscription package to a dealer. Returns 409 if downgrading below the current car or employee count. SITE_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Package assigned",
                    content = @Content(schema = @Schema(implementation = DealerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Missing packageId in request body",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "SITE_ADMIN required",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Dealer or package not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Downgrade would exceed current limits",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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

    @Operation(summary = "Update dealer settings", description = "Updates public-facing dealer settings (display name, description, visibility). SITE_ADMIN can modify any dealer; DEALER_ADMIN only their own. Suspended dealers cannot modify settings.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Settings updated",
                    content = @Content(schema = @Schema(implementation = DealerResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role, wrong dealer, or dealer suspended",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Dealer not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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
