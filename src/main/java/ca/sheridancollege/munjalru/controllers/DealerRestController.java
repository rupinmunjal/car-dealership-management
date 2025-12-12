package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import lombok.AllArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/dealers")
@AllArgsConstructor
public class DealerRestController {
    private final DealerRepository dealerRepository;

    @GetMapping
    public ResponseEntity<List<Dealer>> getAllDealers() {
        return ResponseEntity.ok(dealerRepository.findAll());
    }

    @GetMapping("/{id}")
    public ResponseEntity<Dealer> getDealerById(@PathVariable Long id) {
        return dealerRepository.findById(id)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @GetMapping("/stats/inventory")
    public ResponseEntity<Map<String, Long>> getTotalInventory() {
        long total = dealerRepository.findAll().stream()
                .mapToLong(dealer -> dealer.getCars().size())
                .sum();

        return ResponseEntity.ok(Map.of("totalInventory", total));
    }

    @PostMapping
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Dealer> addDealer(@RequestBody Dealer dealer) {
        try {
            if (dealer.getCars() == null) {
                dealer.setCars(new ArrayList<>());
            }
            Dealer savedDealer = dealerRepository.save(dealer);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDealer);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
        }
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Dealer> updateDealer(@PathVariable Long id, @RequestBody Dealer dealerDetails) {
        return dealerRepository.findById(id)
                .map(dealer -> {
                    dealer.setName(dealerDetails.getName());
                    dealer.setLocation(dealerDetails.getLocation());
                    return ResponseEntity.ok(dealerRepository.save(dealer));
                })
                .orElse(ResponseEntity.notFound().build());
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasAuthority('ADMIN')")
    public ResponseEntity<Void> deleteDealer(@PathVariable Long id) {
        if (dealerRepository.existsById(id)) {
            dealerRepository.deleteById(id);
            return ResponseEntity.ok().build();
        }
        return ResponseEntity.notFound().build();
    }
}