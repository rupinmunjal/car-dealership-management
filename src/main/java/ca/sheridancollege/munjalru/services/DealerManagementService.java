package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.*;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.dto.CreateDealerRequest;
import ca.sheridancollege.munjalru.dto.DealerResponse;
import ca.sheridancollege.munjalru.dto.DealerSettingsRequest;
import ca.sheridancollege.munjalru.mapper.CarDealerMapper;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.annotation.CacheEvict;
import ca.sheridancollege.munjalru.config.CacheConfig;

@Service
@RequiredArgsConstructor
public class DealerManagementService {

    private final DealerRepository dealerRepository;
    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarDealerMapper mapper;
    private final AuditLogService auditLogService;

    /**
     * Creates a new Dealer and its initial DEALER_ADMIN user in one
     * transactional operation.
     */
    @Transactional
    public DealerResponse createDealerWithAdmin(CreateDealerRequest request) {
        if (userRepository.findByEmail(request.getAdminEmail()).isPresent()) {
            throw new IllegalStateException("An account with this email already exists");
        }

        Package pkg = null;
        if (request.getPackageId() != null) {
            pkg = packageRepository.findById(request.getPackageId()).orElse(null);
        }
        if (pkg == null) {
            pkg = packageRepository.findAll().stream().findFirst().orElse(null);
        }

        Dealer dealer = Dealer.builder()
                .name(request.getName())
                .location(request.getLocation())
                .displayName(request.getDisplayName() != null && !request.getDisplayName().isBlank()
                        ? request.getDisplayName() : request.getName())
                .description(request.getDescription())
                .dealerPackage(pkg)
                .status(DealerStatus.ACTIVE)
                .visible(true)
                .build();
        dealer = dealerRepository.save(dealer);

        User admin = User.builder()
                .email(request.getAdminEmail())
                .password(passwordEncoder.encode(request.getAdminPassword()))
                .role(Role.DEALER_ADMIN)
                .dealer(dealer)
                .active(true)
                .dealerStatus(DealerStatus.ACTIVE)
                .build();
        userRepository.save(admin);
        dealer.getUsers().add(admin);

        DealerResponse response = mapper.toDealerResponse(dealer);
        auditLogService.record("DEALER_REGISTERED", "Dealer", dealer.getId(), dealer.getId(), response);
        return response;
    }

    /**
     * Updates a dealer's operational status.
     */
    @Transactional
    public DealerResponse updateStatus(Long dealerId, DealerStatus newStatus) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));
        dealer.setStatus(newStatus);
        DealerResponse response = mapper.toDealerResponse(dealerRepository.save(dealer));
        auditLogService.record("DEALER_STATUS_CHANGED", "Dealer", dealerId, dealerId, response);
        return response;
    }

    /**
     * Assigns a package to a dealer. If the dealer is being downgraded
     * and currently has more active employees than the new package allows,
     * existing employees are NOT retroactively deactivated — only new
     * hires will be blocked.
     */
    @Transactional
    @CacheEvict(cacheNames = CacheConfig.DEALER_DASHBOARD_CACHE, key = "#dealerId")
    public DealerResponse assignPackage(Long dealerId, Long packageId) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));
        Package pkg = packageRepository.findById(packageId)
                .orElseThrow(() -> new EntityNotFoundException("Package not found with id: " + packageId));
        dealer.setDealerPackage(pkg);
        DealerResponse response = mapper.toDealerResponse(dealerRepository.save(dealer));
        auditLogService.record("PACKAGE_ASSIGNED", "Dealer", dealerId, dealerId, response);
        return response;
    }

    /**
     * Updates a dealer's public-facing settings (display name, description, visibility).
     */
    @Transactional
    public DealerResponse updateSettings(Long dealerId, DealerSettingsRequest request) {
        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));

        if (request.getDisplayName() != null) {
            dealer.setDisplayName(request.getDisplayName());
        }
        if (request.getDescription() != null) {
            dealer.setDescription(request.getDescription());
        }
        if (request.getVisible() != null) {
            dealer.setVisible(request.getVisible());
        }
        DealerResponse response = mapper.toDealerResponse(dealerRepository.save(dealer));
        auditLogService.record("DEALER_SETTINGS_UPDATED", "Dealer", dealerId, dealerId, response);
        return response;
    }
}
