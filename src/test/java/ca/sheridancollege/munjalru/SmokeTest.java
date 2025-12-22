package ca.sheridancollege.munjalru;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
// Add this import for the controller check
import ca.sheridancollege.munjalru.config.SecurityConfig; 

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
public class SmokeTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private SecurityConfig securityConfig;

    // TEST 1: Context Loads
    @Test
    public void contextLoads() {
        // Passes if the app starts without crashing
    }

    // TEST 2: Controller/Config Injection
    @Test
    public void securityConfigShouldLoad() {
        assertThat(securityConfig).isNotNull();
    }

    // TEST 3: Health Endpoint
    @Test
    public void shouldReturnHealthStatus() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }
}
