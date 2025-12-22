package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.DealerRequest;
import ca.sheridancollege.munjalru.dto.DealerResponse;
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
}
