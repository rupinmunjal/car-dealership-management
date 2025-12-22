package ca.sheridancollege.munjalru;

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

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SecurityTest extends IntegrationTestBase {

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
        carRepository.deleteAll();
        userRepository.deleteAll();
        dealerRepository.deleteAll();
    }

    private ca.sheridancollege.munjalru.beans.Dealer saveDealer() {
        ca.sheridancollege.munjalru.beans.Dealer dealer = ca.sheridancollege.munjalru.beans.Dealer.builder()
                .name("Test Dealer")
                .location("Toronto")
                .build();
        return dealerRepository.save(dealer);
    }

    @Test
    public void shouldHandleSQLInjectionInCarMake() throws Exception {
        ca.sheridancollege.munjalru.beans.Dealer dealer = saveDealer();

        CarRequest maliciousCar = CarRequest.builder()
                .make("'; DROP TABLE cars; --")
                .model("Injected")
                .modelYear(2024)
                .build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealer.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousCar))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldHandleXSSInDealerName() throws Exception {
        DealerRequest maliciousDealer = DealerRequest.builder()
                .name("<script>alert('XSS')</script>")
                .location("Toronto")
                .build();

        mockMvc.perform(post("/api/v1/dealers")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(maliciousDealer))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().is2xxSuccessful());

        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    public void shouldDenyUnauthorizedAccessQuickly() throws Exception {
        CarRequest car = CarRequest.builder()
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
