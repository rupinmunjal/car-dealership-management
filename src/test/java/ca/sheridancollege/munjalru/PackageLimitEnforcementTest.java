package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.*;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.dto.CarRequest;
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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Tests for package-driven limits: car listing caps and employee seat caps.
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

    @Test
    void dealerAdminCannotExceedCarListingLimit() throws Exception {
        // Add 2 cars (reaching the limit)
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

        // 3rd car should be rejected (limit = 2)
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
        // Remove package from dealer
        dealerA.setDealerPackage(null);
        dealerRepository.save(dealerA);

        // Add 5 cars — all should succeed (no limit)
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
}
