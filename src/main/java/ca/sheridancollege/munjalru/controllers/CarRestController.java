package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/cars")
@AllArgsConstructor
public class CarRestController {
    private final CarRepository carRepository;
    private final DealerRepository dealerRepository;

    @GetMapping
    public ResponseEntity<List<Car>> getAllCars() {
        return ResponseEntity.ok(carRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Car> getCarById(@PathVariable Long id) {
        return carRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats/brands")
    public ResponseEntity<Map<String, Long>> getUniqueBrandsCount() {
        long count = carRepository.findAll().stream()
                .map(Car::getMake)
                .distinct()
                .count();

        return ResponseEntity.ok(Map.of("uniqueBrandsCount", count));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Car> addCar(@RequestBody Car car, @RequestParam Long dealerId) {
        try {
            Dealer dealer = dealerRepository.findById(dealerId)
                    .orElseThrow(() -> new RuntimeException("Dealer not found with id: " + dealerId));

            dealer.getCars().add(car);
            dealerRepository.save(dealer);

            return ResponseEntity.status(HttpStatus.CREATED).body(car);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Car> updateCar(@PathVariable Long id, @RequestBody Car carDetails) {
        return carRepository.findById(id)
                .map(car -> {
                    car.setMake(carDetails.getMake());
                    car.setModel(carDetails.getModel());
                    car.setModelYear(carDetails.getModelYear());
                    return ResponseEntity.ok(carRepository.save(car));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteCar(@PathVariable Long id) {
        if (carRepository.existsById(id)) {
            carRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}