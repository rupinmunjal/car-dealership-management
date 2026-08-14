package ca.sheridancollege.munjalru;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.actuate.observability.AutoConfigureObservability;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.http.MediaType.ALL;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;

@SpringBootTest
@AutoConfigureMockMvc
@AutoConfigureObservability
class ObservabilityEndpointSecurityTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    void healthEndpointRemainsPublic() throws Exception {
        mockMvc.perform(get("/actuator/health"))
                .andExpect(status().isOk());
    }

    @Test
    void prometheusEndpointRejectsAnonymousRequests() throws Exception {
        mockMvc.perform(get("/actuator/prometheus").accept(ALL))
                .andExpect(status().isUnauthorized())
                .andExpect(header().string("WWW-Authenticate", "Basic realm=\"prometheus\""));
    }

    @Test
    void prometheusEndpointRejectsInvalidCredentials() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .with(httpBasic("prometheus", "wrong-password")))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void prometheusEndpointAcceptsScrapeCredentials() throws Exception {
        mockMvc.perform(get("/actuator/prometheus")
                        .with(httpBasic("prometheus", "prometheus-local-password")))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith("text/plain"));
    }

    @Test
    void metricsEndpointStillRequiresAuthentication() throws Exception {
        mockMvc.perform(get("/actuator/metrics"))
                .andExpect(status().isForbidden());
    }
}
