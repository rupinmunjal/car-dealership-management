package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Permission;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.DealerRequest;
import ca.sheridancollege.munjalru.repositories.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Set;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class DealerScopingTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DealerRepository dealerRepository;

    @Autowired
    private UserRepository userRepository;

    private Dealer dealerA;
    private Dealer dealerB;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        dealerRepository.deleteAll();

        dealerA = dealerRepository.save(Dealer.builder()
                .name("Dealer A").location("City A").build());
        dealerB = dealerRepository.save(Dealer.builder()
                .name("Dealer B").location("City B").build());
    }

    // ── DEALER_ADMIN cross-dealer isolation ──────────────────────────

    @Test
    void dealerAdminCannotSeeAnotherDealersCars() throws Exception {
        User adminA = User.builder()
                .email("dadminA@test.com").password("x").role(Role.DEALER_ADMIN).dealer(dealerA).build();
        userRepository.save(adminA);

        CarRequest carForB = CarRequest.builder().make("Toyota").model("Camry").modelYear(2024).build();
        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerB.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(carForB))
                        .with(authentication(dealerAdminAuth(dealerB))))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/cars")
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(0));
    }

    @Test
    void dealerAdminCannotSeeAnotherDealer() throws Exception {
        User adminA = User.builder()
                .email("dadminA@test.com").password("x").role(Role.DEALER_ADMIN).dealer(dealerA).build();
        userRepository.save(adminA);

        mockMvc.perform(get("/api/v1/dealers/" + dealerA.getId())
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isOk());

        mockMvc.perform(get("/api/v1/dealers/" + dealerB.getId())
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isForbidden());
    }

    // ── DEALER_EMPLOYEE permission checks ─────────────────────────────

    @Test
    void dealerEmployeeWithAddPermissionCanAddCar() throws Exception {
        CarRequest car = CarRequest.builder().make("Honda").model("Civic").modelYear(2023).build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(dealerEmployeeAuth(dealerA, Set.of(Permission.CAN_ADD_CAR)))))
                .andExpect(status().isCreated());
    }

    @Test
    void dealerEmployeeWithoutPermissionCannotAddCar() throws Exception {
        CarRequest car = CarRequest.builder().make("Honda").model("Civic").modelYear(2023).build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(dealerEmployeeAuth(dealerA, Set.of()))))
                .andExpect(status().isForbidden());
    }

    // ── SITE_ADMIN access ────────────────────────────────────────────

    @Test
    void siteAdminCanSeeAllDealers() throws Exception {
        mockMvc.perform(get("/api/v1/dealers")
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2));
    }

    @Test
    void siteAdminCanCreateDealer() throws Exception {
        DealerRequest dealer = DealerRequest.builder().name("New Site").location("Somewhere").build();

        mockMvc.perform(post("/api/v1/dealers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealer))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isCreated());
    }

    // ── Unauthorized access ──────────────────────────────────────────

    @Test
    void dealerAdminCannotCreateDealer() throws Exception {
        DealerRequest dealer = DealerRequest.builder().name("Another").location("Somewhere").build();

        mockMvc.perform(post("/api/v1/dealers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(dealer))
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isForbidden());
    }

    // ── Cross-dealer write rejection (scope gap fix) ──────────────────

    @Test
    void dealerAdminCannotAddCarToAnotherDealer() throws Exception {
        CarRequest car = CarRequest.builder().make("Ford").model("Focus").modelYear(2025).build();

        // dealer A tries to POST a car with dealerId=B
        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerB.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerAdminCannotUpdateCarOfAnotherDealer() throws Exception {
        // Create a car in dealer B first (using dealer B's auth)
        CarRequest car = CarRequest.builder().make("BMW").model("X5").modelYear(2024).build();
        String responseJson = mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerB.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(dealerAdminAuth(dealerB))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long carId = objectMapper.readTree(responseJson).get("id").asLong();

        // dealer A tries to update dealer B's car
        CarRequest updated = CarRequest.builder().make("BMW").model("X5").modelYear(2025).build();
        mockMvc.perform(put("/api/v1/cars/" + carId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerAdminCannotDeleteCarOfAnotherDealer() throws Exception {
        // Create a car in dealer B first
        CarRequest car = CarRequest.builder().make("Audi").model("A4").modelYear(2024).build();
        String responseJson = mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerB.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(dealerAdminAuth(dealerB))))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();
        Long carId = objectMapper.readTree(responseJson).get("id").asLong();

        // dealer A tries to delete dealer B's car
        mockMvc.perform(delete("/api/v1/cars/" + carId)
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerAdminCannotUpdateAnotherDealer() throws Exception {
        DealerRequest updated = DealerRequest.builder()
                .name("Hijacked").location("Nowhere").build();

        // dealer A tries to update dealer B
        mockMvc.perform(put("/api/v1/dealers/" + dealerB.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updated))
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerEmployeeCannotAddCarToAnotherDealer() throws Exception {
        CarRequest car = CarRequest.builder().make("Tesla").model("Model 3").modelYear(2024).build();

        // employee of dealer A tries to POST with dealerId=B
        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerB.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(dealerEmployeeAuth(dealerA, Set.of(Permission.CAN_ADD_CAR)))))
                .andExpect(status().isForbidden());
    }

    @Test
    void siteAdminCanAddCarToAnotherDealer() throws Exception {
        // SITE_ADMIN is exempt from dealer scope — can create cars anywhere
        CarRequest car = CarRequest.builder().make("Porsche").model("911").modelYear(2025).build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isCreated());
    }
}
