package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CarResponse;
import ca.sheridancollege.munjalru.mapper.CarDealerMapper;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import ca.sheridancollege.munjalru.config.CacheConfig;

import java.math.BigDecimal;
import java.util.ArrayList;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final DealerRepository dealerRepository;
    private final CarDealerMapper mapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<CarResponse> findAll(String make, String model, BigDecimal minPrice,
                                     BigDecimal maxPrice, Integer year, String search,
                                     Pageable pageable) {
        validatePriceRange(minPrice, maxPrice);
        return carRepository.findAllFiltered(normalize(make), normalize(model), minPrice,
                maxPrice, year, normalize(search), pageable).map(mapper::toCarResponse);
    }

    @Transactional(readOnly = true)
    public Page<CarResponse> findAllByDealer(Long dealerId, String make, String model,
                                             BigDecimal minPrice, BigDecimal maxPrice,
                                             Integer year, String search, Pageable pageable) {
        if (dealerId == null) {
            return Page.empty(pageable);
        }
        if (!dealerRepository.existsById(dealerId)) {
            throw new EntityNotFoundException("Dealer not found with id: " + dealerId);
        }
        validatePriceRange(minPrice, maxPrice);
        return carRepository.findAllFilteredByDealer(dealerId, normalize(make), normalize(model),
                minPrice, maxPrice, year, normalize(search), pageable).map(mapper::toCarResponse);
    }

    @Transactional(readOnly = true)
    public CarResponse findById(Long id) {
        return carRepository.findById(id)
                .map(mapper::toCarResponse)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public CarResponse findByIdAndDealer(Long carId, Long dealerId) {
        if (dealerId == null) {
            throw new EntityNotFoundException("No dealer context for car lookup");
        }
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + carId));
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));
        if (dealer.getCars() == null || !dealer.getCars().contains(car)) {
            throw new EntityNotFoundException("Car not found with id: " + carId);
        }
        return mapper.toCarResponse(car);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.DEALER_DASHBOARD_CACHE, key = "#dealerId")
    public CarResponse createForDealer(Long dealerId, CarRequest request) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));

        // Enforce car listing limit if dealer has a package assigned
        Package pkg = dealer.getDealerPackage();
        if (pkg != null && dealer.getCars() != null) {
            long currentCount = dealer.getCars().size();
            if (currentCount >= pkg.getMaxCarListings()) {
                throw new IllegalStateException(String.format(
                        "Car listing limit (%d) reached for package '%s'. " +
                        "Currently have %d cars. Remove some cars or upgrade your package " +
                        "before adding more.",
                        pkg.getMaxCarListings(), pkg.getName(), currentCount));
            }
        }

        if (dealer.getCars() == null) {
            dealer.setCars(new ArrayList<>());
        }

        Car car = carRepository.save(mapper.toCarEntity(request));
        dealer.getCars().add(car);
        dealerRepository.save(dealer);

        CarResponse response = mapper.toCarResponse(car);
        auditLogService.record("CAR_CREATED", "Car", car.getId(), dealerId, response);
        return response;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.DEALER_DASHBOARD_CACHE, allEntries = true)
    public CarResponse update(Long id, CarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));
        Long dealerId = dealerRepository.findByCarsId(id).map(Dealer::getId).orElse(null);

        mapper.updateCarEntity(car, request);
        CarResponse response = mapper.toCarResponse(carRepository.save(car));
        auditLogService.record("CAR_UPDATED", "Car", id, dealerId, response);
        return response;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.DEALER_DASHBOARD_CACHE, allEntries = true)
    public void delete(Long id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));
        Long dealerId = dealerRepository.findByCarsId(id).map(Dealer::getId).orElse(null);
        CarResponse details = mapper.toCarResponse(car);
        carRepository.deleteById(id);
        auditLogService.record("CAR_DELETED", "Car", id, dealerId, details);
    }

    @Transactional(readOnly = true)
    public long countUniqueBrands() {
        return carRepository.findAll().stream()
                .map(Car::getMake)
                .distinct()
                .count();
    }

    /**
     * Returns {@code true} if the car with {@code carId} belongs to the dealer
     * with {@code dealerId}. Used for write-endpoint dealer-scope enforcement.
     */
    @Transactional(readOnly = true)
    public boolean belongsToDealer(Long carId, Long dealerId) {
        if (carId == null || dealerId == null) {
            return false;
        }
        return dealerRepository.findByCarsId(carId)
                .map(dealer -> dealer.getId().equals(dealerId))
                .orElse(false);
    }

    private static String normalize(String value) {
        return value == null || value.isBlank() ? null : value.trim();
    }

    private static void validatePriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        if (minPrice != null && minPrice.signum() < 0) {
            throw new IllegalArgumentException("minPrice cannot be negative");
        }
        if (maxPrice != null && maxPrice.signum() < 0) {
            throw new IllegalArgumentException("maxPrice cannot be negative");
        }
        if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
            throw new IllegalArgumentException("minPrice cannot be greater than maxPrice");
        }
    }
}
