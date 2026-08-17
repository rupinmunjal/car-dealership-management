package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class PackageApiIT extends RestAssuredIntegrationTestBase {

    @Test
    void performsPackageCrudWithPagination() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        String token = login("site-admin@example.com");

        long packageId = authorized(token)
                .body(Map.of(
                        "name", "Growth",
                        "maxEmployeeSeats", 10,
                        "maxCarListings", 50))
                .post("/api/v1/packages")
                .then()
                .statusCode(201)
                .body("name", equalTo("Growth"))
                .extract().jsonPath().getLong("id");

        authorized(token)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .get("/api/v1/packages")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("totalElements", equalTo(1));

        authorized(token)
                .get("/api/v1/packages/{id}", packageId)
                .then()
                .statusCode(200)
                .body("maxCarListings", equalTo(50));

        authorized(token)
                .body(Map.of(
                        "name", "Growth Plus",
                        "maxEmployeeSeats", 15,
                        "maxCarListings", 75))
                .put("/api/v1/packages/{id}", packageId)
                .then()
                .statusCode(200)
                .body("name", equalTo("Growth Plus"))
                .body("maxEmployeeSeats", equalTo(15));

        authorized(token)
                .delete("/api/v1/packages/{id}", packageId)
                .then()
                .statusCode(200);

        authorized(token)
                .get("/api/v1/packages/{id}", packageId)
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"));
    }

    @Test
    void enforcesPackageValidationAndSiteAdminRole() {
        Dealer dealer = createDealer("Package Role Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        createUser("dealer-admin@example.com", Role.DEALER_ADMIN, dealer);

        authorized(login("site-admin@example.com"))
                .body(Map.of(
                        "name", "",
                        "maxEmployeeSeats", 0,
                        "maxCarListings", 0))
                .post("/api/v1/packages")
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Failed"))
                .body("message.name", equalTo("Package name is required"));

        authorized(login("dealer-admin@example.com"))
                .get("/api/v1/packages")
                .then()
                .statusCode(403)
                .body("error", equalTo("Forbidden"));
    }
}
