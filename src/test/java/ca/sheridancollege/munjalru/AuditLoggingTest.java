package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.AuditLog;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.repositories.AuditLogRepository;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
class AuditLoggingTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private AuditLogRepository auditLogRepository;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DealerRepository dealerRepository;

    private Dealer dealerA;
    private Dealer dealerB;

    @BeforeEach
    void setUp() {
        auditLogRepository.deleteAll();
        carRepository.deleteAll();
        userRepository.deleteAll();
        dealerRepository.deleteAll();
        dealerA = dealerRepository.save(Dealer.builder().name("Audit A").location("Toronto")
                .status(DealerStatus.ACTIVE).visible(true).build());
        dealerB = dealerRepository.save(Dealer.builder().name("Audit B").location("Oakville")
                .status(DealerStatus.ACTIVE).visible(true).build());
    }

    @Test
    void carMutationRecordsActorDealerAndJsonDetails() throws Exception {
        CarRequest request = CarRequest.builder()
                .make("Honda").model("Civic").modelYear(2025).build();

        mockMvc.perform(post("/api/v1/cars")
                        .param("dealerId", dealerA.getId().toString())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isCreated());

        AuditLog log = auditLogRepository.findAll().getFirst();
        assertEquals("CAR_CREATED", log.getAction());
        assertEquals(dealerA.getId(), log.getDealerId());
        assertEquals(2L, log.getUserId());
        assertTrue(log.getDetails().contains("Honda"));
    }

    @Test
    void dealerAdminOnlySeesOwnDealerActivity() throws Exception {
        auditLogRepository.save(AuditLog.builder().dealerId(dealerA.getId())
                .action("CAR_CREATED").entityType("Car").entityId(1L).details("{}").build());
        auditLogRepository.save(AuditLog.builder().dealerId(dealerB.getId())
                .action("CAR_DELETED").entityType("Car").entityId(2L).details("{}").build());
        auditLogRepository.save(AuditLog.builder().action("PACKAGE_CREATED")
                .entityType("Package").entityId(3L).details("{}").build());

        mockMvc.perform(get("/api/v1/audit-logs")
                        .with(authentication(dealerAdminAuth(dealerA))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(1))
                .andExpect(jsonPath("$.content[0].dealerId").value(dealerA.getId()));

        mockMvc.perform(get("/api/v1/audit-logs")
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalElements").value(3));
    }
}
