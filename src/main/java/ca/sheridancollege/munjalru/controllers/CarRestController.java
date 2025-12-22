package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CarResponse;
import ca.sheridancollege.munjalru.services.CarService;
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
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
public class CarRestController {

    private final CarService carService;

    /**
     * Returns cars scoped to the caller's dealer.
     * SITE_ADMIN sees all cars; DEALER_ADMIN/EMPLOYEE see only their dealer's.
     */
    @GetMapping
    public ResponseEntity<Page<CarResponse>> getAllCars(
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == Role.SITE_ADMIN) {
            return ResponseEntity.ok(carService.findAll(pageable));
        }
        Long dealerId = currentUser.getDealer() != null
                ? currentUser.getDealer().getId()
                : null;
        return ResponseEntity.ok(carService.findAllByDealer(dealerId, pageable));
    }

    /**
     * Returns a single car. SITE_ADMIN can access any car.
     * DEALER_ADMIN/EMPLOYEE can only access cars belonging to their dealer.
     */
    @GetMapping("/{id}")
    public ResponseEntity<CarResponse> getCarById(@PathVariable Long id,
                                                  @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == Role.SITE_ADMIN) {
            return ResponseEntity.ok(carService.findById(id));
        }
        Long dealerId = currentUser.getDealer() != null
                ? currentUser.getDealer().getId()
                : null;
        return ResponseEntity.ok(carService.findByIdAndDealer(id, dealerId));
    }

    @GetMapping("/stats/brands")
    public ResponseEntity<Map<String, Long>> getUniqueBrandsCount() {
        return ResponseEntity.ok(Map.of("uniqueBrandsCount", carService.countUniqueBrands()));
    }

    @PostMapping
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN') or hasAuthority('CAN_ADD_CAR')")
    public ResponseEntity<CarResponse> addCar(@Valid @RequestBody CarRequest carRequest,
                                              @RequestParam Long dealerId,
                                              @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN
                && (currentUser.getDealer() == null
                || !dealerId.equals(currentUser.getDealer().getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(carService.createForDealer(dealerId, carRequest));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN') or hasAuthority('CAN_EDIT_CAR')")
    public ResponseEntity<CarResponse> updateCar(@PathVariable Long id,
                                                 @Valid @RequestBody CarRequest carRequest,
                                                 @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN
                && (currentUser.getDealer() == null
                || !carService.belongsToDealer(id, currentUser.getDealer().getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(carService.update(id, carRequest));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN') or hasAuthority('CAN_DELETE_CAR')")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id,
                                          @AuthenticationPrincipal User currentUser) {
        if (currentUser == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        if (currentUser.getRole() != Role.SITE_ADMIN
                && (currentUser.getDealer() == null
                || !carService.belongsToDealer(id, currentUser.getDealer().getId()))) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        carService.delete(id);
        return ResponseEntity.ok().build();
    }
}
