package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Role;
import org.junit.jupiter.api.Test;
import org.springframework.test.context.TestPropertySource;

import java.util.Map;

import static org.hamcrest.Matchers.containsString;
import static org.hamcrest.Matchers.equalTo;

@TestPropertySource(properties = {
        "app.rate-limit.enabled=true",
        "app.rate-limit.general-per-minute=2",
        "app.rate-limit.auth-per-minute=2"
})
class RateLimitApiIT extends RestAssuredIntegrationTestBase {

    @Test
    void returns429ForAuthenticationAndGeneralApiLimits() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        String token = login("site-admin@example.com");

        request()
                .body(Map.of("email", "site-admin@example.com", "password", "WrongPassword123!"))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(401);

        request()
                .body(Map.of("email", "site-admin@example.com", "password", "WrongPassword123!"))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(429)
                .header("Retry-After", "60")
                .body("status", equalTo(429))
                .body("error", equalTo("Too Many Requests"))
                .body("message", containsString("Maximum 2 requests per minute"));

        authorized(token).get("/api/v1/cars").then().statusCode(200);
        authorized(token).get("/api/v1/cars").then().statusCode(200);
        authorized(token)
                .get("/api/v1/cars")
                .then()
                .statusCode(429)
                .header("Retry-After", "60")
                .body("status", equalTo(429));
    }
}
