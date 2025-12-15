package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
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
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.ArrayList;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FunctionalTest {

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

    @BeforeEach
    public void setup() {
        // Clean up before each test
        carRepository.deleteAll();
        dealerRepository.deleteAll();
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
        // First register a user
        String userJson = """
            {
                "email": "logintest@example.com",
                "password": "TestPass123!"
            }
            """;
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(userJson));

        // Then login with same credentials
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(userJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").exists());
    }

    // TEST 3: Reject login with invalid password
    @Test
    public void shouldRejectLoginWithInvalidPassword() throws Exception {
        // First register a user
        String registerJson = """
            {
                "email": "wrongpass@example.com",
                "password": "CorrectPass123!"
            }
            """;
        
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(registerJson));

        // Try to login with wrong password
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

    // TEST 4: Add car with valid data (requires ADMIN role)
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void shouldAddCarWithValidData() throws Exception {
        // First create a dealer
        Dealer dealer = Dealer.builder()
                .name("Test Dealer")
                .location("Toronto, ON")
                .cars(new ArrayList<>())
                .build();
        dealer = dealerRepository.save(dealer);

        Car car = Car.builder()
                .make("Toyota")
                .model("Camry")
                .modelYear(2024)
                .build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Toyota"))
                .andExpect(jsonPath("$.model").value("Camry"))
                .andExpect(jsonPath("$.modelYear").value(2024));
    }

    // TEST 5: Update car with valid data
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void shouldUpdateCarWithValidData() throws Exception {
        // Create dealer and car
        Dealer dealer = Dealer.builder()
                .name("Test Dealer")
                .location("Toronto, ON")
                .cars(new ArrayList<>())
                .build();
        
        Car car = Car.builder()
                .make("Ford")
                .model("F-150")
                .modelYear(2020)
                .build();
        
        dealer.getCars().add(car);
        dealer = dealerRepository.save(dealer);
        Car savedCar = dealer.getCars().get(0);

        Car updatedCar = Car.builder()
                .make("Ford")
                .model("F-150")
                .modelYear(2024)
                .build();

        mockMvc.perform(put("/api/v1/cars/" + savedCar.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updatedCar)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.modelYear").value(2024));
    }

    // TEST 6: Delete car
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void shouldDeleteCar() throws Exception {
        // Create dealer and car
        Dealer dealer = Dealer.builder()
                .name("Test Dealer")
                .location("Toronto, ON")
                .cars(new ArrayList<>())
                .build();
        
        Car car = Car.builder()
                .make("Tesla")
                .model("Model 3")
                .modelYear(2023)
                .build();
        
        dealer.getCars().add(car);
        dealer = dealerRepository.save(dealer);
        Car savedCar = dealer.getCars().get(0);

        mockMvc.perform(delete("/api/v1/cars/" + savedCar.getId()))
                .andExpect(status().isOk());
    }
}
