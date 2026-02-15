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
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.Collections;

@Service
@RequiredArgsConstructor
public class CarService {

    private final CarRepository carRepository;
    private final DealerRepository dealerRepository;
    private final CarDealerMapper mapper;

    @Transactional(readOnly = true)
    public Page<CarResponse> findAll(Pageable pageable) {
        return carRepository.findAll(pageable).map(mapper::toCarResponse);
    }

    @Transactional(readOnly = true)
    public Page<CarResponse> findAllByDealer(Long dealerId, Pageable pageable) {
        if (dealerId == null) {
            return Page.empty(pageable);
        }
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));
        if (dealer.getCars() == null) {
            return new PageImpl<>(Collections.emptyList(), pageable, 0);
        }
        return new PageImpl<>(dealer.getCars().stream()
                .map(mapper::toCarResponse)
                .toList(), pageable, dealer.getCars().size());
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

        return mapper.toCarResponse(car);
    }

    @Transactional
    public CarResponse update(Long id, CarRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Car not found with id: " + id));

        mapper.updateCarEntity(car, request);
        return mapper.toCarResponse(carRepository.save(car));
    }

    @Transactional
    public void delete(Long id) {
        if (!carRepository.existsById(id)) {
            throw new EntityNotFoundException("Car not found with id: " + id);
        }
        carRepository.deleteById(id);
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
}
