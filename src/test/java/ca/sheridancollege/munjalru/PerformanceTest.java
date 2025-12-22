package ca.sheridancollege.munjalru;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;

@SpringBootTest
@AutoConfigureMockMvc
public class PerformanceTest extends IntegrationTestBase {

    @Autowired
    private MockMvc mockMvc;

    @Test
    public void healthCheckShouldBeFast() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/actuator/health"));

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 500, "Health check too slow: " + duration + "ms");
    }

    @Test
    public void indexShouldLoadFast() throws Exception {
        long startTime = System.currentTimeMillis();

        mockMvc.perform(get("/index.html"));

        long duration = System.currentTimeMillis() - startTime;
        assertTrue(duration < 500, "Index page too slow: " + duration + "ms");
    }
}
