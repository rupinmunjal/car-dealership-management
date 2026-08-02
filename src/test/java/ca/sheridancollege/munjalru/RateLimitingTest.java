package ca.sheridancollege.munjalru;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@TestPropertySource(properties = {
        "app.rate-limit.enabled=true",
        "app.rate-limit.general-per-minute=2",
        "app.rate-limit.auth-per-minute=1"
})
class RateLimitingTest extends IntegrationTestBase {

    @Autowired private MockMvc mockMvc;

    @Test
    void appliesStricterLimitToAuthenticationEndpoints() throws Exception {
        String invalidLogin = "{\"email\":\"invalid\",\"password\":\"short\"}";

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isBadRequest());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(invalidLogin))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "60"))
                .andExpect(jsonPath("$.status").value(429));
    }

    @Test
    void limitsGeneralApiTrafficPerClientAddress() throws Exception {
        mockMvc.perform(get("/api/v1/cars").with(authentication(siteAdminAuth())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/cars").with(authentication(siteAdminAuth())))
                .andExpect(status().isOk());
        mockMvc.perform(get("/api/v1/cars").with(authentication(siteAdminAuth())))
                .andExpect(status().isTooManyRequests())
                .andExpect(jsonPath("$.error").value("Too Many Requests"));
    }
}
