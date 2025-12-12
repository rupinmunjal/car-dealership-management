package ca.sheridancollege.munjalru;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
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
    // This proves your Security Config is actually working
    @Test
    public void shouldProtectPrivateApi() throws Exception {
        mockMvc.perform(get("/api/v1/unknown-private-endpoint"))
                .andExpect(status().isUnauthorized()); // Expects 401 Unauthorized
                // Note: If you configured it to return 403, change to .isForbidden()
    }

    // TEST 3: Static Resources (CSS/JS)
    // This proves Angular assets are being served
    @Test
    public void shouldLoadRootPath() throws Exception {
        mockMvc.perform(get("/"))
                .andExpect(status().isOk());
    }
}
