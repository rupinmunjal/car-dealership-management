package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.config.CacheConfig;
import ca.sheridancollege.munjalru.dto.DealerDashboardSummary;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class DealerDashboardService {

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;

    @Cacheable(cacheNames = CacheConfig.DEALER_DASHBOARD_CACHE, key = "#dealerId")
    @Transactional(readOnly = true)
    public DealerDashboardSummary getSummary(Long dealerId) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));
        Package dealerPackage = dealer.getDealerPackage();

        return DealerDashboardSummary.builder()
                .dealerId(dealer.getId())
                .dealerName(dealer.getDisplayName() == null ? dealer.getName() : dealer.getDisplayName())
                .location(dealer.getLocation())
                .carCount(dealer.getCars() == null ? 0 : dealer.getCars().size())
                .employeeCount(userRepository.countByDealerIdAndRoleAndActiveTrue(
                        dealerId, Role.DEALER_EMPLOYEE))
                .packageName(dealerPackage == null ? null : dealerPackage.getName())
                .maxEmployeeSeats(dealerPackage == null ? null : dealerPackage.getMaxEmployeeSeats())
                .maxCarListings(dealerPackage == null ? null : dealerPackage.getMaxCarListings())
                .build();
    }
}
