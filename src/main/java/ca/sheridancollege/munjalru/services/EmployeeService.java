package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.*;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.dto.CreateEmployeeRequest;
import ca.sheridancollege.munjalru.dto.EmployeeResponse;
import ca.sheridancollege.munjalru.mapper.CarDealerMapper;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.cache.annotation.CacheEvict;
import ca.sheridancollege.munjalru.config.CacheConfig;

import java.time.Instant;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class EmployeeService {

    private final UserRepository userRepository;
    private final DealerRepository dealerRepository;
    private final PasswordEncoder passwordEncoder;
    private final CarDealerMapper mapper;
    private final AuditLogService auditLogService;

    @Transactional(readOnly = true)
    public Page<EmployeeResponse> listEmployees(Long dealerId, Pageable pageable) {
        return userRepository.findByDealerIdAndRoleAndActiveTrue(
                dealerId, Role.DEALER_EMPLOYEE, pageable).map(mapper::toEmployeeResponse);
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.DEALER_DASHBOARD_CACHE, key = "#dealerId")
    public EmployeeResponse createEmployee(Long dealerId, CreateEmployeeRequest request) {
        if (userRepository.findByEmail(request.getEmail()).isPresent()) {
            throw new IllegalStateException("An account with this email already exists");
        }

        Dealer dealer = dealerRepository.findById(dealerId)
                .orElseThrow(() -> new EntityNotFoundException("Dealer not found with id: " + dealerId));

        Package pkg = dealer.getDealerPackage();
        if (pkg != null) {
            long currentCount = userRepository.countByDealerIdAndRoleAndActiveTrue(dealerId, Role.DEALER_EMPLOYEE);
            if (currentCount >= pkg.getMaxEmployeeSeats()) {
                throw new IllegalStateException(String.format(
                        "Employee seat limit (%d) reached for package '%s'. " +
                        "Currently have %d active employees. Upgrade your package or " +
                        "deactivate %d employee(s) before adding more.",
                        pkg.getMaxEmployeeSeats(), pkg.getName(), currentCount,
                        currentCount - pkg.getMaxEmployeeSeats() + 1));
            }
        }

        Set<Permission> permissions = request.getPermissions().stream()
                .map(Permission::valueOf)
                .collect(Collectors.toSet());

        User employee = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DEALER_EMPLOYEE)
                .dealer(dealer)
                .permissions(permissions)
                .active(true)
                .dealerStatus(dealer.getStatus())
                .build();
        EmployeeResponse response = mapper.toEmployeeResponse(userRepository.save(employee));
        auditLogService.record("EMPLOYEE_HIRED", "User", employee.getId(), dealerId, response);
        return response;
    }

    @Transactional
    public EmployeeResponse updatePermissions(Long dealerId, Long employeeId, Set<String> permissionNames) {
        User employee = findEmployeeInDealer(employeeId, dealerId);

        Set<Permission> permissions = permissionNames.stream()
                .map(Permission::valueOf)
                .collect(Collectors.toSet());
        employee.setPermissions(permissions);
        EmployeeResponse response = mapper.toEmployeeResponse(userRepository.save(employee));
        auditLogService.record("EMPLOYEE_PERMISSIONS_CHANGED", "User", employeeId,
                dealerId, response);
        return response;
    }

    @Transactional
    @CacheEvict(cacheNames = CacheConfig.DEALER_DASHBOARD_CACHE, key = "#dealerId")
    public void deactivateEmployee(Long dealerId, Long employeeId) {
        User employee = findEmployeeInDealer(employeeId, dealerId);
        employee.setActive(false);
        employee.setDeletedAt(Instant.now());
        userRepository.delete(employee);
        auditLogService.record("EMPLOYEE_DEACTIVATED", "User", employeeId,
                dealerId, mapper.toEmployeeResponse(employee));
    }

    private User findEmployeeInDealer(Long employeeId, Long dealerId) {
        return userRepository.findByIdAndDealerId(employeeId, dealerId)
                .orElseThrow(() -> new EntityNotFoundException(
                        "Employee not found with id: " + employeeId + " for dealer: " + dealerId));
    }
}
