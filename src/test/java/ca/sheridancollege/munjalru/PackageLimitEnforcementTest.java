package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.*;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CreateEmployeeRequest;
import ca.sheridancollege.munjalru.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Tests for package-driven limits: car listing caps, employee seat caps,
 * and package downgrade behaviour.
 */
@SpringBootTest
@AutoConfigureMockMvc
public class PackageLimitEnforcementTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DealerRepository dealerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PackageRepository packageRepository;

    private Dealer dealerA;
    private Package smallPkg;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        dealerRepository.deleteAll();
        packageRepository.deleteAll();

        smallPkg = packageRepository.save(Package.builder()
                .name("Starter")
                .maxEmployeeSeats(1)
                .maxCarListings(2)
                .build());

        dealerA = dealerRepository.save(Dealer.builder()
                .name("Dealer A").location("City A")
                .dealerPackage(smallPkg)
                .build());
    }

    private UsernamePasswordAuthenticationToken dealerAdminAuth() {
        User user = new User();
        user.setId(10L);
        user.setEmail("dadminA@test.com");
        user.setRole(Role.DEALER_ADMIN);
        user.setDealer(dealerA);
        user.setDealerStatus(DealerStatus.ACTIVE);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    // ── Car listing limit enforcement ──────────────────────────────

    @Test
    void dealerAdminCannotExceedCarListingLimit() throws Exception {
        for (int i = 1; i <= 2; i++) {
            CarRequest car = CarRequest.builder()
                    .make("Brand" + i).model("Model" + i).modelYear(2024).build();
            mockMvc.perform(post("/api/v1/cars")
                            .param("dealerId", dealerA.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(car))
                            .with(authentication(dealerAdminAuth())))
                    .andExpect(status().isCreated());
        }

        CarRequest car = CarRequest.builder()
                .make("Brand3").model("Model3").modelYear(2024).build();
        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isConflict());
    }

    @Test
    void dealerWithoutPackageHasNoCarLimit() throws Exception {
        dealerA.setDealerPackage(null);
        dealerRepository.save(dealerA);

        for (int i = 1; i <= 5; i++) {
            CarRequest car = CarRequest.builder()
                    .make("Brand" + i).model("Model" + i).modelYear(2024).build();
            mockMvc.perform(post("/api/v1/cars")
                            .param("dealerId", dealerA.getId().toString())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(car))
                            .with(authentication(dealerAdminAuth())))
                    .andExpect(status().isCreated());
        }
    }

    // ── Package downgrade behaviour ────────────────────────────────

    @Test
    void packageDowngradeDoesNotDeactivateExistingEmployees() throws Exception {
        CreateEmployeeRequest emp = CreateEmployeeRequest.builder()
                .email("emp1@test.com").password("pw").permissions(Set.of()).build();
        mockMvc.perform(post("/api/v1/dealers/" + dealerA.getId() + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isCreated());

        Package zeroPkg = packageRepository.save(Package.builder()
                .name("Zero").maxEmployeeSeats(0).maxCarListings(5).build());
        dealerA.setDealerPackage(zeroPkg);
        dealerRepository.save(dealerA);

        mockMvc.perform(get("/api/v1/dealers/" + dealerA.getId() + "/employees")
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(1));
    }

    @Test
    void packageDowngradeBlocksNewHires() throws Exception {
        CreateEmployeeRequest emp = CreateEmployeeRequest.builder()
                .email("emp1@test.com").password("pw").permissions(Set.of()).build();
        mockMvc.perform(post("/api/v1/dealers/" + dealerA.getId() + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isCreated());

        Package zeroPkg = packageRepository.save(Package.builder()
                .name("Zero").maxEmployeeSeats(0).maxCarListings(5).build());
        dealerA.setDealerPackage(zeroPkg);
        dealerRepository.save(dealerA);

        CreateEmployeeRequest emp2 = CreateEmployeeRequest.builder()
                .email("emp2@test.com").password("pw").permissions(Set.of()).build();
        mockMvc.perform(post("/api/v1/dealers/" + dealerA.getId() + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp2))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isConflict());
    }
}
