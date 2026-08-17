package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.contains;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class CarApiIT extends RestAssuredIntegrationTestBase {

    @Test
    void performsCarCrudThroughRealHttpAndPersistence() {
        Dealer dealer = createDealer("CRUD Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        String token = login("site-admin@example.com");

        long carId = authorized(token)
                .queryParam("dealerId", dealer.getId())
                .body(Map.of(
                        "make", "Toyota",
                        "model", "Camry",
                        "modelYear", 2024,
                        "price", 34000))
                .post("/api/v1/cars")
                .then()
                .statusCode(201)
                .body("make", equalTo("Toyota"))
                .extract()
                .jsonPath()
                .getLong("id");

        authorized(token)
                .get("/api/v1/cars/{id}", carId)
                .then()
                .statusCode(200)
                .body("model", equalTo("Camry"));

        authorized(token)
                .body(Map.of(
                        "make", "Toyota",
                        "model", "Camry Hybrid",
                        "modelYear", 2025,
                        "price", 39000))
                .put("/api/v1/cars/{id}", carId)
                .then()
                .statusCode(200)
                .body("model", equalTo("Camry Hybrid"))
                .body("modelYear", equalTo(2025));

        authorized(token)
                .delete("/api/v1/cars/{id}", carId)
                .then()
                .statusCode(200);

        authorized(token)
                .get("/api/v1/cars/{id}", carId)
                .then()
                .statusCode(404)
                .body("status", equalTo(404));
    }

    @Test
    void supportsPaginationSortingAndFiltering() {
        Dealer dealer = createDealer("Search Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        createCar(dealer, "Toyota", "Camry", 2024, "35000.00");
        createCar(dealer, "Toyota", "Corolla", 2022, "25000.00");
        createCar(dealer, "Honda", "Civic", 2023, "28000.00");
        String token = login("site-admin@example.com");

        authorized(token)
                .queryParam("make", "toy")
                .queryParam("minPrice", "20000")
                .queryParam("maxPrice", "40000")
                .queryParam("page", 0)
                .queryParam("size", 1)
                .queryParam("sort", "price,desc")
                .get("/api/v1/cars")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content.make", contains("Toyota"))
                .body("content.model", contains("Camry"))
                .body("totalElements", equalTo(2))
                .body("totalPages", equalTo(2))
                .body("size", equalTo(1));

        authorized(token)
                .queryParam("year", 2023)
                .queryParam("search", "civ")
                .get("/api/v1/cars")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].model", equalTo("Civic"));
    }

    @Test
    void returnsInventoryStatistics() {
        Dealer dealer = createDealer("Stats Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        createCar(dealer, "Toyota", "Camry", 2024, "35000.00");
        createCar(dealer, "Toyota", "Corolla", 2023, "27000.00");
        createCar(dealer, "Honda", "Civic", 2022, "25000.00");
        String token = login("site-admin@example.com");

        authorized(token)
                .get("/api/v1/cars/stats/brands")
                .then()
                .statusCode(200)
                .body("uniqueBrandsCount", equalTo(2));

        authorized(token)
                .get("/api/v1/dealers/stats/inventory")
                .then()
                .statusCode(200)
                .body("totalInventory", equalTo(3));
    }

    @Test
    void returnsBadRequestForValidationPriceRangeAndMalformedJson() {
        Dealer dealer = createDealer("Validation Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        String token = login("site-admin@example.com");

        authorized(token)
                .queryParam("dealerId", dealer.getId())
                .body(Map.of("make", "", "model", "Model", "modelYear", 1800, "price", -1))
                .post("/api/v1/cars")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Validation Failed"))
                .body("message.make", equalTo("Make is required"))
                .body("message.modelYear", equalTo("Model year must be 1900 or later"))
                .body("message.price", equalTo("Price cannot be negative"));

        authorized(token)
                .queryParam("minPrice", 50000)
                .queryParam("maxPrice", 10000)
                .get("/api/v1/cars")
                .then()
                .statusCode(400)
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("minPrice cannot be greater than maxPrice"));

        authorized(token)
                .queryParam("dealerId", dealer.getId())
                .body("{\"make\":\"Toyota\"")
                .post("/api/v1/cars")
                .then()
                .statusCode(400)
                .body("status", equalTo(400))
                .body("error", equalTo("Bad Request"))
                .body("message", equalTo("Malformed JSON request"));
    }

    @Test
    void returnsNotFoundForUnknownCarAndDealer() {
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        String token = login("site-admin@example.com");

        authorized(token)
                .get("/api/v1/cars/{id}", 999999)
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"));

        authorized(token)
                .queryParam("dealerId", 999999)
                .body(Map.of("make", "Toyota", "model", "Camry", "modelYear", 2024))
                .post("/api/v1/cars")
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"));
    }
}
