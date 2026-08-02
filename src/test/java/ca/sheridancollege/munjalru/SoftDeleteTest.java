package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CarResponse;
import ca.sheridancollege.munjalru.dto.CreateEmployeeRequest;
import ca.sheridancollege.munjalru.dto.EmployeeResponse;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import ca.sheridancollege.munjalru.services.CarService;
import ca.sheridancollege.munjalru.services.EmployeeService;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest
class SoftDeleteTest extends IntegrationTestBase {

    @Autowired private CarService carService;
    @Autowired private EmployeeService employeeService;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DealerRepository dealerRepository;
    @Autowired private PackageRepository packageRepository;
    @Autowired private JdbcTemplate jdbcTemplate;

    private Dealer dealer;

    @BeforeEach
    void setUp() {
        dealerRepository.deleteAll();
        packageRepository.deleteAll();

        Package singleSeatPackage = packageRepository.save(Package.builder()
                .name("Soft Delete Test")
                .maxEmployeeSeats(1)
                .maxCarListings(1)
                .build());
        dealer = dealerRepository.save(Dealer.builder()
                .name("Retention Motors")
                .location("Toronto")
                .status(DealerStatus.ACTIVE)
                .visible(true)
                .dealerPackage(singleSeatPackage)
                .build());
    }

    @Test
    void deletingCarRetainsRowButFreesListingCapacity() {
        CarResponse deletedCar = carService.createForDealer(dealer.getId(), CarRequest.builder()
                .make("Honda").model("Civic").modelYear(2024).build());

        carService.delete(deletedCar.getId());

        assertTrue(carRepository.findById(deletedCar.getId()).isEmpty());
        assertTrue(dealerRepository.findByCarsId(deletedCar.getId()).isEmpty());
        assertNotNull(jdbcTemplate.queryForObject(
                "SELECT deleted_at FROM car WHERE id = ?", Object.class, deletedCar.getId()));

        CarResponse replacement = carService.createForDealer(dealer.getId(), CarRequest.builder()
                .make("Toyota").model("Corolla").modelYear(2025).build());

        assertNotNull(replacement.getId());
        assertEquals(1, carRepository.count());
        assertEquals(2, jdbcTemplate.queryForObject("SELECT COUNT(*) FROM car", Long.class));
    }

    @Test
    void deletingEmployeeHidesAccountAndFreesSeatCapacity() {
        EmployeeResponse deletedEmployee = employeeService.createEmployee(dealer.getId(),
                employeeRequest("employee@test.local"));

        employeeService.deactivateEmployee(dealer.getId(), deletedEmployee.getId());

        assertTrue(userRepository.findById(deletedEmployee.getId()).isEmpty());
        assertTrue(userRepository.findByEmail(deletedEmployee.getEmail()).isEmpty());
        assertEquals(0, userRepository.countByDealerIdAndRoleAndActiveTrue(
                dealer.getId(), Role.DEALER_EMPLOYEE));
        assertNotNull(jdbcTemplate.queryForObject(
                "SELECT deleted_at FROM _user WHERE id = ?", Object.class, deletedEmployee.getId()));
        assertFalse(jdbcTemplate.queryForObject(
                "SELECT active FROM _user WHERE id = ?", Boolean.class, deletedEmployee.getId()));
        assertThrows(EntityNotFoundException.class,
                () -> employeeService.updatePermissions(dealer.getId(), deletedEmployee.getId(), Set.of()));

        EmployeeResponse replacement = employeeService.createEmployee(dealer.getId(),
                employeeRequest("employee@test.local"));

        assertNotNull(replacement.getId());
        assertTrue(replacement.isActive());
        assertEquals(1, userRepository.countByDealerIdAndRoleAndActiveTrue(
                dealer.getId(), Role.DEALER_EMPLOYEE));
        assertEquals(2, jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM _user WHERE email = 'employee@test.local'", Long.class));
    }

    private static CreateEmployeeRequest employeeRequest(String email) {
        return CreateEmployeeRequest.builder()
                .email(email)
                .password("SoftDelete123!")
                .permissions(Set.of())
                .build();
    }
}
