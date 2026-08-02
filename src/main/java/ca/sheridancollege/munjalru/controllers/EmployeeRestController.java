package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.CreateEmployeeRequest;
import ca.sheridancollege.munjalru.dto.EmployeeResponse;
import ca.sheridancollege.munjalru.dto.UpdatePermissionsRequest;
import ca.sheridancollege.munjalru.exception.ApiError;
import ca.sheridancollege.munjalru.services.EmployeeService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;

@RestController
@RequestMapping("/api/v1/dealers/{dealerId}/employees")
@RequiredArgsConstructor
@Tag(name = "Employees", description = "Employee CRUD and permission management within a dealer")
@SecurityRequirement(name = "bearerAuth")
public class EmployeeRestController {

    private final EmployeeService employeeService;

    @Operation(summary = "List employees", description = "Returns all employees for a given dealer. SITE_ADMIN and DEALER_ADMIN only. DEALER_ADMIN must belong to the dealer; suspended dealers cannot access.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "List of employees"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role, wrong dealer, or dealer suspended",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<Page<EmployeeResponse>> listEmployees(
            @Parameter(description = "Dealer ID") @PathVariable Long dealerId,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN) {
            if (currentUser.getDealer() == null || !dealerId.equals(currentUser.getDealer().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (currentUser.getDealerStatus() == DealerStatus.SUSPENDED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(employeeService.listEmployees(dealerId, pageable));
    }

    @Operation(summary = "Create employee", description = "Creates a new employee under a dealer. SITE_ADMIN and DEALER_ADMIN only. Returns 409 if seat limit exceeded or email already in use. Suspended dealers cannot create employees.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Employee created",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role, wrong dealer, or dealer suspended",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "409", description = "Seat limit exceeded or email already in use",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @Parameter(description = "Dealer ID") @PathVariable Long dealerId,
            @Valid @RequestBody CreateEmployeeRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN) {
            if (currentUser.getDealer() == null || !dealerId.equals(currentUser.getDealer().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (currentUser.getDealerStatus() == DealerStatus.SUSPENDED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(employeeService.createEmployee(dealerId, request));
    }

    @Operation(summary = "Update employee permissions", description = "Updates the permission set for an employee. SITE_ADMIN and DEALER_ADMIN only. Permissions determine which actions the employee can perform (e.g. CAN_ADD_CAR, CAN_EDIT_CAR, CAN_DELETE_CAR).")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Permissions updated",
                    content = @Content(schema = @Schema(implementation = EmployeeResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role, wrong dealer, or dealer suspended",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found or wrong dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PutMapping("/{employeeId}/permissions")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<EmployeeResponse> updatePermissions(
            @Parameter(description = "Dealer ID") @PathVariable Long dealerId,
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @Valid @RequestBody UpdatePermissionsRequest request,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN) {
            if (currentUser.getDealer() == null || !dealerId.equals(currentUser.getDealer().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (currentUser.getDealerStatus() == DealerStatus.SUSPENDED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        return ResponseEntity.ok(employeeService.updatePermissions(dealerId, employeeId, request.getPermissions()));
    }

    @Operation(summary = "Deactivate employee", description = "Soft-deletes the employee while retaining the database row. The account is hidden and cannot authenticate. SITE_ADMIN and DEALER_ADMIN only.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Employee deactivated"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role, wrong dealer, or dealer suspended",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Employee not found or wrong dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<Void> deactivateEmployee(
            @Parameter(description = "Dealer ID") @PathVariable Long dealerId,
            @Parameter(description = "Employee ID") @PathVariable Long employeeId,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN) {
            if (currentUser.getDealer() == null || !dealerId.equals(currentUser.getDealer().getId())) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
            if (currentUser.getDealerStatus() == DealerStatus.SUSPENDED) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
            }
        }
        employeeService.deactivateEmployee(dealerId, employeeId);
        return ResponseEntity.ok().build();
    }
}
