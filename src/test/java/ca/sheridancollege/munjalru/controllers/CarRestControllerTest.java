package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CarResponse;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import ca.sheridancollege.munjalru.services.CarService;
import ca.sheridancollege.munjalru.services.JwtService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.web.servlet.MockMvc;

import org.springframework.test.context.ActiveProfiles;

import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class CarRestControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private CarService carService;

    @MockBean
    private JwtService jwtService;

    @MockBean
    private CarRepository carRepository;

    @MockBean
    private DealerRepository dealerRepository;

    @MockBean
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        // Ensure mocked repos return empty defaults
        when(carRepository.findAll()).thenReturn(List.of());
        when(dealerRepository.findAll()).thenReturn(List.of());
        when(userRepository.findAll()).thenReturn(List.of());
    }

    private static UsernamePasswordAuthenticationToken siteAdminAuth() {
        User siteAdmin = new User();
        siteAdmin.setId(1L);
        siteAdmin.setEmail("admin@test.com");
        siteAdmin.setRole(Role.SITE_ADMIN);
        return new UsernamePasswordAuthenticationToken(
                siteAdmin, null, siteAdmin.getAuthorities());
    }

    @Test
    void shouldReturnPagedCars() throws Exception {
        CarResponse car = CarResponse.builder()
                .id(1L).make("Toyota").model("Camry").modelYear(2024).build();
        Page<CarResponse> page = new PageImpl<>(List.of(car));

        when(carService.findAll(any(Pageable.class))).thenReturn(page);

        mockMvc.perform(get("/api/v1/cars")
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[0].make").value("Toyota"));
    }

    @Test
    void shouldCreateCar() throws Exception {
        CarRequest request = CarRequest.builder()
                .make("Honda").model("Civic").modelYear(2023).build();

        CarResponse response = CarResponse.builder()
                .id(1L).make("Honda").model("Civic").modelYear(2023).build();

        when(carService.createForDealer(anyLong(), any(CarRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.make").value("Honda"));
    }

    @Test
    void shouldRejectInvalidCarRequest() throws Exception {
        CarRequest request = CarRequest.builder()
                .make("").model("Model").modelYear(2024).build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", "1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isBadRequest());
    }
}
