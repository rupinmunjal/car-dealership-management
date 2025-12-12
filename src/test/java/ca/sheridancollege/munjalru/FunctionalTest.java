package ca.sheridancollege.munjalru;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
public class FunctionalTest {

    @Autowired
    private MockMvc mockMvc;

    // TEST 1: Public Index Page
    @Test
    public void shouldLoadIndexPage() throws Exception {
        mockMvc.perform(get("/index.html"))
                .andExpect(status().isOk());
    }

    // TEST 2: Protected Endpoint (Should Fail/Redirect)
    @Test
    public void shouldProtectPrivateApi() throws Exception {
        mockMvc.perform(get("/api/v1/unknown-private-endpoint"))
                .andExpect(status().isForbidden()); // Expects 403 Forbidden (Spring Security default for anonymous users)
    }

    // TEST 3: Static Resources (CSS/JS)
    @Test
    public void shouldLoadRootPath() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
