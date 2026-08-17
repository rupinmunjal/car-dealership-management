package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.empty;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;
import static org.hamcrest.Matchers.blankOrNullString;

class AuthenticationApiIT extends RestAssuredIntegrationTestBase {

    @Test
    void registersAUserAndAllowsImmediateAuthentication() {
        request()
                .body(Map.of("email", "new-user@example.com", "password", TEST_PASSWORD))
                .post("/api/v1/auth/register")
                .then()
                .statusCode(200)
                .body("token", not(blankOrNullString()))
                .body("role", equalTo("DEALER_EMPLOYEE"))
                .body("dealerId", equalTo(null))
                .body("permissions", empty());

        request()
                .body(Map.of("email", "new-user@example.com", "password", TEST_PASSWORD))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("role", equalTo("DEALER_EMPLOYEE"));
    }

    @Test
    void authenticatesValidCredentialsAndReturnsJwtClaims() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);

        request()
                .body(Map.of("email", "site-admin@example.com", "password", TEST_PASSWORD))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .body("token", not(blankOrNullString()))
                .body("role", equalTo("SITE_ADMIN"))
                .body("permissions", empty());
    }

    @Test
    void rejectsInvalidCredentials() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);

        request()
                .body(Map.of("email", "site-admin@example.com", "password", "WrongPassword123!"))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(401)
                .body("status", equalTo(401))
                .body("error", equalTo("Unauthorized"));
    }

    @Test
    void rejectsInvalidLoginRequest() {
        request()
                .body(Map.of("email", "not-an-email", "password", "short"))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Validation Failed"))
                .body("message.email", equalTo("Email must be valid"))
                .body("message.password", equalTo("Password must be at least 6 characters"));
    }

    @Test
    void handlesMissingInvalidAndExpiredJwt() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);

        request()
                .get("/api/v1/cars")
                .then()
                .statusCode(401)
                .header("WWW-Authenticate", "Bearer")
                .body("status", equalTo(401))
                .body("error", equalTo("Unauthorized"));

        authorized("not-a-jwt")
                .get("/api/v1/cars")
                .then()
                .statusCode(401);

        authorized(tokenWithInvalidSignature("site-admin@example.com", Role.SITE_ADMIN))
                .get("/api/v1/cars")
                .then()
                .statusCode(401);

        authorized(expiredToken("site-admin@example.com", Role.SITE_ADMIN))
                .get("/api/v1/cars")
                .then()
                .statusCode(401);
    }
}
