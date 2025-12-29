package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.CreateEmployeeRequest;
import ca.sheridancollege.munjalru.dto.EmployeeResponse;
import ca.sheridancollege.munjalru.dto.UpdatePermissionsRequest;
import ca.sheridancollege.munjalru.services.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/dealers/{dealerId}/employees")
@RequiredArgsConstructor
public class EmployeeRestController {

    private final EmployeeService employeeService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<List<EmployeeResponse>> listEmployees(
            @PathVariable Long dealerId,
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
        return ResponseEntity.ok(employeeService.listEmployees(dealerId));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<EmployeeResponse> createEmployee(
            @PathVariable Long dealerId,
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

    @PutMapping("/{employeeId}/permissions")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<EmployeeResponse> updatePermissions(
            @PathVariable Long dealerId,
            @PathVariable Long employeeId,
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

    @DeleteMapping("/{employeeId}")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<Void> deactivateEmployee(
            @PathVariable Long dealerId,
            @PathVariable Long employeeId,
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
