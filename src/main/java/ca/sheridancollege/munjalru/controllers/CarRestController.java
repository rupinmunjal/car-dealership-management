package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CarResponse;
import ca.sheridancollege.munjalru.exception.ApiError;
import ca.sheridancollege.munjalru.services.CarService;
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
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/v1/cars")
@RequiredArgsConstructor
@Tag(name = "Cars", description = "CRUD for car inventory — scoped by dealer and permissions")
@SecurityRequirement(name = "bearerAuth")
public class CarRestController {

    private final CarService carService;

    @Operation(summary = "List all cars", description = "Returns cars scoped to the caller. SITE_ADMIN sees all cars; DEALER_ADMIN and DEALER_EMPLOYEE see only their dealer's inventory.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Paginated list of cars"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping
    public ResponseEntity<Page<CarResponse>> getAllCars(
            @RequestParam(required = false) String make,
            @RequestParam(required = false) String model,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Integer year,
            @RequestParam(required = false) String search,
            @ParameterObject @PageableDefault(size = 20, sort = "id") Pageable pageable,
            @AuthenticationPrincipal User currentUser) {
        if (currentUser.getRole() == Role.SITE_ADMIN) {
            return ResponseEntity.ok(carService.findAll(make, model, minPrice, maxPrice,
                    year, search, pageable));
        }
        Long dealerId = currentUser.getDealer() != null
                ? currentUser.getDealer().getId()
                : null;
        return ResponseEntity.ok(carService.findAllByDealer(dealerId, make, model, minPrice,
                maxPrice, year, search, pageable));
    }

    @Operation(summary = "Get car by ID", description = "Returns a single car. SITE_ADMIN can access any car; others are scoped to their dealer. Returns 404 if not found or out of scope.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Car found",
                    content = @Content(schema = @Schema(implementation = CarResponse.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Car belongs to another dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Car not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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

    @Operation(summary = "Count unique car brands", description = "Returns the total number of distinct car brands across all dealerships. Requires authentication.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Brand count"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @GetMapping("/stats/brands")
    public ResponseEntity<Map<String, Long>> getUniqueBrandsCount() {
        return ResponseEntity.ok(Map.of("uniqueBrandsCount", carService.countUniqueBrands()));
    }

    @Operation(summary = "Add a car", description = "Creates a new car for the specified dealer. Requires SITE_ADMIN, DEALER_ADMIN, or CAN_ADD_CAR permission. Non-admin users can only add to their own dealer.")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Car created",
                    content = @Content(schema = @Schema(implementation = CarResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role/permission or wrong dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
    @PostMapping
    @PreAuthorize("hasAnyAuthority('SITE_ADMIN', 'DEALER_ADMIN') or hasAuthority('CAN_ADD_CAR')")
    public ResponseEntity<CarResponse> addCar(@Valid @RequestBody CarRequest carRequest,
                                              @Parameter(description = "ID of the dealer to add the car to") @RequestParam Long dealerId,
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

    @Operation(summary = "Update a car", description = "Updates an existing car. Requires SITE_ADMIN, DEALER_ADMIN, or CAN_EDIT_CAR permission. Non-admin users can only edit their own dealer's cars.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Car updated",
                    content = @Content(schema = @Schema(implementation = CarResponse.class))),
            @ApiResponse(responseCode = "400", description = "Validation failed",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role/permission or wrong dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Car not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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

    @Operation(summary = "Delete a car", description = "Deletes a car by ID. Requires SITE_ADMIN, DEALER_ADMIN, or CAN_DELETE_CAR permission. Non-admin users can only delete their own dealer's cars.")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Car deleted"),
            @ApiResponse(responseCode = "401", description = "Missing or invalid JWT",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "403", description = "Insufficient role/permission or wrong dealer",
                    content = @Content(schema = @Schema(implementation = ApiError.class))),
            @ApiResponse(responseCode = "404", description = "Car not found",
                    content = @Content(schema = @Schema(implementation = ApiError.class)))
    })
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
