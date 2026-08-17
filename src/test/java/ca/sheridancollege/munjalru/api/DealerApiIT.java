package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class DealerApiIT extends RestAssuredIntegrationTestBase {

    @Test
    void performsDealerCrudAndReturnsDashboardSummary() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        String token = login("site-admin@example.com");

        long dealerId = authorized(token)
                .body(Map.of("name", "HTTP Motors", "location", "Toronto"))
                .post("/api/v1/dealers")
                .then()
                .statusCode(201)
                .body("name", equalTo("HTTP Motors"))
                .extract().jsonPath().getLong("id");

        authorized(token)
                .queryParam("size", 1)
                .get("/api/v1/dealers")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("totalElements", equalTo(1));

        authorized(token)
                .get("/api/v1/dealers/{id}", dealerId)
                .then()
                .statusCode(200)
                .body("location", equalTo("Toronto"));

        authorized(token)
                .body(Map.of("name", "HTTP Motors Updated", "location", "Ottawa"))
                .put("/api/v1/dealers/{id}", dealerId)
                .then()
                .statusCode(200)
                .body("name", equalTo("HTTP Motors Updated"));

        authorized(token)
                .get("/api/v1/dealers/{id}/dashboard-summary", dealerId)
                .then()
                .statusCode(200)
                .body("dealerId", equalTo((int) dealerId))
                .body("carCount", equalTo(0))
                .body("employeeCount", equalTo(0));

        authorized(token)
                .delete("/api/v1/dealers/{id}", dealerId)
                .then()
                .statusCode(200);

        authorized(token)
                .get("/api/v1/dealers/{id}", dealerId)
                .then()
                .statusCode(404);
    }

    @Test
    void managesDealerRegistrationStatusPackageAndSettings() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        ca.sheridancollege.munjalru.beans.Package starter = createPackage("Starter", 5, 25);
        ca.sheridancollege.munjalru.beans.Package growth = createPackage("Growth", 20, 100);
        String token = login("site-admin@example.com");

        long dealerId = authorized(token)
                .body(Map.of(
                        "name", "Managed Motors",
                        "location", "Mississauga",
                        "adminEmail", "managed-admin@example.com",
                        "adminPassword", TEST_PASSWORD,
                        "packageId", starter.getId(),
                        "displayName", "Managed Auto"))
                .post("/api/v1/dealers/register")
                .then()
                .statusCode(201)
                .body("adminEmail", equalTo("managed-admin@example.com"))
                .body("dealerPackage.name", equalTo("Starter"))
                .extract().jsonPath().getLong("id");

        authorized(token)
                .body(Map.of("packageId", growth.getId()))
                .put("/api/v1/dealers/{id}/package", dealerId)
                .then()
                .statusCode(200)
                .body("dealerPackage.name", equalTo("Growth"));

        authorized(token)
                .body(Map.of("status", "SUSPENDED"))
                .put("/api/v1/dealers/{id}/status", dealerId)
                .then()
                .statusCode(200)
                .body("status", equalTo("SUSPENDED"));

        authorized(token)
                .body(Map.of(
                        "displayName", "Managed Auto Group",
                        "description", "Updated through the API",
                        "visible", false))
                .put("/api/v1/dealers/{id}/settings", dealerId)
                .then()
                .statusCode(200)
                .body("displayName", equalTo("Managed Auto Group"))
                .body("visible", equalTo(false));
    }

    @Test
    void returnsDealerValidationConflictAndNotFoundResponses() {
        Dealer dealer = createDealer("Existing Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        createUser("existing-admin@example.com", Role.DEALER_ADMIN, dealer);
        String token = login("site-admin@example.com");

        authorized(token)
                .body(Map.of("name", "", "location", ""))
                .post("/api/v1/dealers")
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Failed"));

        authorized(token)
                .body(Map.of(
                        "name", "Duplicate Admin Motors",
                        "location", "Toronto",
                        "adminEmail", "existing-admin@example.com",
                        "adminPassword", TEST_PASSWORD))
                .post("/api/v1/dealers/register")
                .then()
                .statusCode(409)
                .body("error", equalTo("Conflict"));

        authorized(token)
                .body(Map.of("status", "PAUSED"))
                .put("/api/v1/dealers/{id}/status", dealer.getId())
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"));

        authorized(token)
                .body(Map.of())
                .put("/api/v1/dealers/{id}/package", dealer.getId())
                .then()
                .statusCode(400);

        authorized(token)
                .get("/api/v1/dealers/{id}", 999999)
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"));
    }
}
