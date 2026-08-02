package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.AuditLogResponse;
import ca.sheridancollege.munjalru.services.AuditLogService;
import lombok.RequiredArgsConstructor;
import org.springdoc.core.annotations.ParameterObject;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/audit-logs")
@RequiredArgsConstructor
public class AuditLogRestController {

    private final AuditLogService auditLogService;

    @GetMapping
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN')")
    public ResponseEntity<Page<AuditLogResponse>> getAuditLogs(
            @ParameterObject @PageableDefault(size = 20, sort = "timestamp",
                    direction = org.springframework.data.domain.Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() == Role.SITE_ADMIN) {
            return ResponseEntity.ok(auditLogService.findAll(pageable));
        }
        if (currentUser.getDealer() == null) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(auditLogService.findByDealer(
                currentUser.getDealer().getId(), pageable));
    }
}
