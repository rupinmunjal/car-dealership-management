package ca.sheridancollege.munjalru.mapper;

import ca.sheridancollege.munjalru.beans.*;
import ca.sheridancollege.munjalru.dto.*;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Component
public class CarDealerMapper {

    // ── Car ──────────────────────────────────────────────────────

    public CarResponse toCarResponse(Car car) {
        if (car == null) {
            return null;
        }
        return CarResponse.builder()
                .id(car.getId())
                .make(car.getMake())
                .model(car.getModel())
                .modelYear(car.getModelYear())
                .price(car.getPrice())
                .build();
    }

    public Car toCarEntity(CarRequest request) {
        if (request == null) {
            return null;
        }
        return Car.builder()
                .make(request.getMake())
                .model(request.getModel())
                .modelYear(request.getModelYear())
                .price(request.getPrice())
                .build();
    }

    public void updateCarEntity(Car car, CarRequest request) {
        if (car == null || request == null) {
            return;
        }
        car.setMake(request.getMake());
        car.setModel(request.getModel());
        car.setModelYear(request.getModelYear());
        car.setPrice(request.getPrice());
    }

    // ── Dealer ───────────────────────────────────────────────────

    public DealerResponse toDealerResponse(Dealer dealer) {
        if (dealer == null) {
            return null;
        }
        List<CarResponse> carResponses = dealer.getCars() == null
                ? List.of()
                : dealer.getCars().stream().map(this::toCarResponse).toList();
        List<EmployeeResponse> employeeResponses = dealer.getUsers() == null
                ? List.of()
                : dealer.getUsers().stream()
                        .filter(user -> user.getRole() == Role.DEALER_EMPLOYEE)
                        .map(this::toEmployeeResponse)
                        .toList();
        String adminEmail = dealer.getUsers() == null
                ? null
                : dealer.getUsers().stream()
                        .filter(user -> user.getRole() == Role.DEALER_ADMIN)
                        .map(User::getEmail)
                        .findFirst()
                        .orElse(null);
        return DealerResponse.builder()
                .id(dealer.getId())
                .name(dealer.getName())
                .adminEmail(adminEmail)
                .location(dealer.getLocation())
                .status(dealer.getStatus())
                .displayName(dealer.getDisplayName())
                .description(dealer.getDescription())
                .visible(dealer.isVisible())
                .dealerPackage(toPackageResponse(dealer.getDealerPackage()))
                .cars(carResponses)
                .employees(employeeResponses)
                .build();
    }

    public Dealer toDealerEntity(DealerRequest request) {
        if (request == null) {
            return null;
        }
        return Dealer.builder()
                .name(request.getName())
                .location(request.getLocation())
                .build();
    }

    public void updateDealerEntity(Dealer dealer, DealerRequest request) {
        if (dealer == null || request == null) {
            return;
        }
        dealer.setName(request.getName());
        dealer.setLocation(request.getLocation());
    }

    // ── Package ──────────────────────────────────────────────────

    public PackageResponse toPackageResponse(ca.sheridancollege.munjalru.beans.Package pkg) {
        if (pkg == null) {
            return null;
        }
        return PackageResponse.builder()
                .id(pkg.getId())
                .name(pkg.getName())
                .maxEmployeeSeats(pkg.getMaxEmployeeSeats())
                .maxCarListings(pkg.getMaxCarListings())
                .build();
    }

    public ca.sheridancollege.munjalru.beans.Package toPackageEntity(PackageRequest request) {
        if (request == null) {
            return null;
        }
        return ca.sheridancollege.munjalru.beans.Package.builder()
                .name(request.getName())
                .maxEmployeeSeats(request.getMaxEmployeeSeats())
                .maxCarListings(request.getMaxCarListings())
                .build();
    }

    public void updatePackageEntity(ca.sheridancollege.munjalru.beans.Package pkg, PackageRequest request) {
        if (pkg == null || request == null) {
            return;
        }
        pkg.setName(request.getName());
        pkg.setMaxEmployeeSeats(request.getMaxEmployeeSeats());
        pkg.setMaxCarListings(request.getMaxCarListings());
    }

    // ── Employee ─────────────────────────────────────────────────

    public EmployeeResponse toEmployeeResponse(User user) {
        if (user == null) {
            return null;
        }
        Set<String> permNames = user.getPermissions() == null
                ? Set.of()
                : user.getPermissions().stream().map(Enum::name).collect(Collectors.toSet());
        return EmployeeResponse.builder()
                .id(user.getId())
                .email(user.getEmail())
                .role(user.getRole().name())
                .active(user.isActive())
                .permissions(permNames)
                .build();
    }
}
