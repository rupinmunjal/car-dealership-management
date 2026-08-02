package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class CarPaginationTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private CarRepository carRepository;
    @Autowired private DealerRepository dealerRepository;
    @Autowired private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        dealerRepository.deleteAll();
        carRepository.deleteAll();

        carRepository.save(Car.builder().make("Toyota").model("Camry")
                .modelYear(2022).price(new BigDecimal("24500")).build());
        carRepository.save(Car.builder().make("Toyota").model("Corolla")
                .modelYear(2024).price(new BigDecimal("31000")).build());
        carRepository.save(Car.builder().make("Honda").model("Civic")
                .modelYear(2024).price(new BigDecimal("29000")).build());
    }

    @Test
    void paginatesAndSearchesAcrossMakeAndModel() throws Exception {
        mockMvc.perform(get("/api/v1/cars")
                        .param("search", "toy")
                        .param("page", "0")
                        .param("size", "1")
                        .param("sort", "modelYear,desc")
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(2))
                .andExpect(jsonPath("$.size").value(1))
                .andExpect(jsonPath("$.content[0].model").value("Corolla"));
    }

    @Test
    void combinesStructuredCarFilters() throws Exception {
        mockMvc.perform(get("/api/v1/cars")
                        .param("make", "honda")
                        .param("model", "civ")
                        .param("year", "2024")
                        .param("minPrice", "28000")
                        .param("maxPrice", "30000")
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].make").value("Honda"));
    }

    @Test
    void rejectsAnInvertedPriceRange() throws Exception {
        mockMvc.perform(get("/api/v1/cars")
                        .param("minPrice", "30000")
                        .param("maxPrice", "20000")
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("minPrice cannot be greater than maxPrice"));
    }
}
