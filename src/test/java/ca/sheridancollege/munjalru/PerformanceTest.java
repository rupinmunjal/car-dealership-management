package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class PerformanceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private CarRepository carRepository;

    @Autowired
    private DealerRepository dealerRepository;

    @BeforeEach
    public void setup() {
        carRepository.deleteAll();
        dealerRepository.deleteAll();
    }

    // TEST 1: Health Check Speed
    @Test
    public void healthCheckShouldBeFast() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/actuator/health"));
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 500, "Health check too slow: " + duration + "ms");
    }

    // TEST 2: Index Page Load Speed
    @Test
    public void indexShouldLoadFast() throws Exception {
        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(get("/index.html"));
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 500, "Index page too slow: " + duration + "ms");
    }

    // TEST 3: SQL Injection Prevention in Car Creation
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void shouldHandleSQLInjectionInCarMake() throws Exception {
        // Create a dealer first
        Dealer dealer = Dealer.builder()
                .name("Test Dealer")
                .location("Toronto")
                .cars(new ArrayList<>())
                .build();
        dealer = dealerRepository.save(dealer);

        // Attempt SQL injection in make field
        Car maliciousCar = Car.builder()
                .make("'; DROP TABLE cars; --")
                .model("Injected")
                .modelYear(2024)
                .build();

        // Should either reject or safely store the input
        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousCar)))
                .andExpect(status().is2xxSuccessful());

        // Verify database is still intact
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // TEST 4: XSS Prevention in Dealer Name
    @Test
    @WithMockUser(username = "admin", authorities = {"ADMIN"})
    public void shouldHandleXSSInDealerName() throws Exception {
        Dealer maliciousDealer = Dealer.builder()
                .name("<script>alert('XSS')</script>")
                .location("Toronto")
                .build();

        // Should either reject or escape the input
        mockMvc.perform(post("/api/v1/dealers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousDealer)))
                .andExpect(status().is2xxSuccessful());

        // Verify the system is still functional
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    // TEST 5: Unauthorized Access Should Be Denied Quickly
    @Test
    public void shouldDenyUnauthorizedAccessQuickly() throws Exception {
        Car car = Car.builder()
                .make("Toyota")
                .model("Camry")
                .modelYear(2024)
                .build();

        long startTime = System.currentTimeMillis();
        
        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(car)))
                .andExpect(status().isForbidden());
        
        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 200, "Authorization check too slow: " + duration + "ms");
    }
}
