package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.*;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.dto.*;
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

@SpringBootTest
@AutoConfigureMockMvc
public class Phase2ManagementTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;
    @Autowired private ObjectMapper objectMapper;
    @Autowired private DealerRepository dealerRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private PackageRepository packageRepository;

    private Dealer dealerA;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
        dealerRepository.deleteAll();
        packageRepository.deleteAll();

        dealerA = dealerRepository.save(Dealer.builder()
                .name("Dealer A").location("City A").build());
    }

    // ── helpers ────────────────────────────────────────────────────

    private UsernamePasswordAuthenticationToken dealerAdminAuth() {
        User user = new User();
        user.setId(10L);
        user.setEmail("dadminA@test.com");
        user.setRole(Role.DEALER_ADMIN);
        user.setDealer(dealerA);
        user.setDealerStatus(DealerStatus.ACTIVE);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    private UsernamePasswordAuthenticationToken suspendedDealerAdminAuth() {
        User user = new User();
        user.setId(11L);
        user.setEmail("suspended@test.com");
        user.setRole(Role.DEALER_ADMIN);
        user.setDealer(dealerA);
        user.setDealerStatus(DealerStatus.SUSPENDED);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    // ── SITE_ADMIN-only endpoints reject DEALER_ADMIN ──────────────

    @Test
    void dealerAdminCannotCreatePackage() throws Exception {
        PackageRequest pkg = PackageRequest.builder()
                .name("Pro").maxEmployeeSeats(5).maxCarListings(20).build();
        mockMvc.perform(post("/api/v1/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(pkg))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerAdminCannotRegisterDealer() throws Exception {
        CreateDealerRequest req = CreateDealerRequest.builder()
                .name("New").location("Here").adminEmail("a@b.com").adminPassword("pw").build();
        mockMvc.perform(post("/api/v1/dealers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void registeredDealerResponsesIncludeAdminEmail() throws Exception {
        CreateDealerRequest req = CreateDealerRequest.builder()
                .name("Email Motors")
                .location("Toronto")
                .adminEmail("admin@emailmotors.test")
                .adminPassword("password")
                .build();

        mockMvc.perform(post("/api/v1/dealers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.adminEmail").value("admin@emailmotors.test"));

        mockMvc.perform(get("/api/v1/dealers")
                        .with(authentication(siteAdminAuth())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content[1].adminEmail").value("admin@emailmotors.test"));
    }

    @Test
    void dealerAdminCannotUpdateDealerStatus() throws Exception {
        DealerStatusRequest req = DealerStatusRequest.builder().status("SUSPENDED").build();
        mockMvc.perform(put("/api/v1/dealers/" + dealerA.getId() + "/status")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerAdminCannotAssignPackage() throws Exception {
        mockMvc.perform(put("/api/v1/dealers/" + dealerA.getId() + "/package")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"packageId\":1}")
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    // ── Seat limit enforcement ─────────────────────────────────────

    @Test
    void dealerAdminCannotCreateEmployeeBeyondSeatLimit() throws Exception {
        Package pkg = packageRepository.save(Package.builder()
                .name("Tiny").maxEmployeeSeats(1).maxCarListings(5).build());
        dealerA.setDealerPackage(pkg);
        dealerRepository.save(dealerA);

        // Create one employee (fills the single seat)
        CreateEmployeeRequest emp = CreateEmployeeRequest.builder()
                .email("emp1@test.com").password("pw").permissions(Set.of()).build();
        mockMvc.perform(post("/api/v1/dealers/" + dealerA.getId() + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isCreated());

        // Try to create a second employee — should be rejected with 409
        CreateEmployeeRequest emp2 = CreateEmployeeRequest.builder()
                .email("emp2@test.com").password("pw").permissions(Set.of()).build();
        mockMvc.perform(post("/api/v1/dealers/" + dealerA.getId() + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp2))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isConflict());
    }

    // ── Suspended dealer users are blocked ─────────────────────────

    @Test
    void suspendedDealerAdminCannotCreateEmployee() throws Exception {
        CreateEmployeeRequest emp = CreateEmployeeRequest.builder()
                .email("emp@test.com").password("pw").permissions(Set.of()).build();
        mockMvc.perform(post("/api/v1/dealers/" + dealerA.getId() + "/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(emp))
                        .with(authentication(suspendedDealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void suspendedDealerAdminCannotUpdateSettings() throws Exception {
        DealerSettingsRequest req = DealerSettingsRequest.builder()
                .displayName("Hijacked").build();
        mockMvc.perform(put("/api/v1/dealers/" + dealerA.getId() + "/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(suspendedDealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    // ── Cross-dealer scoping ───────────────────────────────────────

    @Test
    void dealerAdminCannotManageAnotherDealersEmployees() throws Exception {
        Dealer dealerB = dealerRepository.save(Dealer.builder()
                .name("Dealer B").location("City B").build());

        // Dealer A tries to list employees of Dealer B
        mockMvc.perform(get("/api/v1/dealers/" + dealerB.getId() + "/employees")
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerAdminCannotUpdateAnotherDealersSettings() throws Exception {
        Dealer dealerB = dealerRepository.save(Dealer.builder()
                .name("Dealer B").location("City B").build());

        DealerSettingsRequest req = DealerSettingsRequest.builder()
                .displayName("Hijacked").build();
        mockMvc.perform(put("/api/v1/dealers/" + dealerB.getId() + "/settings")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(dealerAdminAuth())))
                .andExpect(status().isForbidden());
    }

    // ── Null principal rejection ──────────────────────────────────

    @Test
    void nullPrincipalRejectedOnEmployeeEndpoint() throws Exception {
        mockMvc.perform(get("/api/v1/dealers/" + dealerA.getId() + "/employees"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void nullPrincipalRejectedOnPackageEndpoint() throws Exception {
        mockMvc.perform(post("/api/v1/packages")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"X\",\"maxEmployeeSeats\":1,\"maxCarListings\":1}"))
                .andExpect(status().isUnauthorized());
    }

    // ── DEALER_EMPLOYEE also rejected from SITE_ADMIN endpoints ──

    @Test
    void dealerEmployeeCannotAccessPackages() throws Exception {
        User emp = new User();
        emp.setId(20L);
        emp.setEmail("emp@test.com");
        emp.setRole(Role.DEALER_EMPLOYEE);
        emp.setDealer(dealerA);
        emp.setDealerStatus(DealerStatus.ACTIVE);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                emp, null, emp.getAuthorities());

        mockMvc.perform(get("/api/v1/packages")
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }

    @Test
    void dealerEmployeeCannotRegisterDealer() throws Exception {
        User emp = new User();
        emp.setId(21L);
        emp.setEmail("emp2@test.com");
        emp.setRole(Role.DEALER_EMPLOYEE);
        emp.setDealer(dealerA);
        emp.setDealerStatus(DealerStatus.ACTIVE);
        var auth = new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(
                emp, null, emp.getAuthorities());

        CreateDealerRequest req = CreateDealerRequest.builder()
                .name("X").location("Y").adminEmail("a@b.com").adminPassword("pw").build();
        mockMvc.perform(post("/api/v1/dealers/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(req))
                        .with(authentication(auth)))
                .andExpect(status().isForbidden());
    }
}
