package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.DealerRequest;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
public class FunctionalTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DealerRepository dealerRepository;

    @SuppressWarnings("unused")
    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    public void setup() {
        carRepository.deleteAll();
        userRepository.deleteAll();
        dealerRepository.deleteAll();
    }

    private Dealer saveDealer() {
        Dealer dealer = Dealer.builder()
                .name("Test Dealer")
                .location("Toronto, ON")
                .build();
        return dealerRepository.save(dealer);
    }

    // TEST 1: Valid user registration
    @Test
    public void shouldRegisterUserWithValidData() throws Exception {
        String validUserJson = """
            {
                "email": "newuser@example.com",
                "password": "SecurePass123!"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(validUserJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists())
                .andExpect(jsonPath("$.role").exists());
    }

    // TEST 2: Valid user login
    @Test
    public void shouldAuthenticateWithValidCredentials() throws Exception {
        String userJson = """
            {
                "email": "logintest@example.com",
                "password": "TestPass123!"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson));

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    // TEST 3: Reject login with invalid password
    @Test
    public void shouldRejectLoginWithInvalidPassword() throws Exception {
        String registerJson = """
            {
                "email": "wrongpass@example.com",
                "password": "CorrectPass123!"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        String wrongPassJson = """
            {
                "email": "wrongpass@example.com",
                "password": "WrongPass123!"
            }
            """;

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(wrongPassJson))
                .andExpect(status().is4xxClientError());
    }

    // TEST 4: Add car with valid data (requires SITE_ADMIN)
    @Test
    public void shouldAddCarWithValidData() throws Exception {
        Dealer dealer = saveDealer();

        CarRequest car = CarRequest.builder()
                .make("Toyota")
                .model("Camry")
                .modelYear(2024)
                .build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.modelYear").value(2024));
    }

    // TEST 5: Update car with valid data
    @Test
    public void shouldUpdateCarWithValidData() throws Exception {
        Dealer dealer = saveDealer();

        CarRequest car = CarRequest.builder()
                .make("Ford")
                .model("F-150")
                .modelYear(2020)
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(siteAdminAuth())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long savedCarId = objectMapper.readTree(createResponse).path("id").asLong();

        CarRequest updatedCar = CarRequest.builder()
                .make("Ford")
                .model("F-150")
                .modelYear(2024)
                .build();

        mockMvc.perform(put("/api/v1/cars/" + savedCarId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCar))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelYear").value(2024));
    }

    // TEST 6: Delete car
    @Test
    public void shouldDeleteCar() throws Exception {
        Dealer dealer = saveDealer();

        CarRequest car = CarRequest.builder()
                .make("Tesla")
                .model("Model 3")
                .modelYear(2023)
                .build();

        String createResponse = mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car))
                        .with(authentication(siteAdminAuth())))
                .andReturn()
                .getResponse()
                .getContentAsString();

        Long savedCarId = objectMapper.readTree(createResponse).path("id").asLong();

        mockMvc.perform(delete("/api/v1/cars/" + savedCarId)
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk());
    }
}
