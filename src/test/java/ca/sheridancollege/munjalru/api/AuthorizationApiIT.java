package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Permission;
import ca.sheridancollege.munjalru.beans.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class AuthorizationApiIT extends RestAssuredIntegrationTestBase {

    @Test
    void enforcesThreeTierRoleAccess() {
        Dealer dealer = createDealer("Role Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        createUser("dealer-admin@example.com", Role.DEALER_ADMIN, dealer);
        createUser("employee@example.com", Role.DEALER_EMPLOYEE, dealer);

        authorized(login("site-admin@example.com"))
                .get("/api/v1/packages")
                .then()
                .statusCode(200);

        authorized(login("dealer-admin@example.com"))
                .get("/api/v1/packages")
                .then()
                .statusCode(403);

        authorized(login("employee@example.com"))
                .get("/api/v1/packages")
                .then()
                .statusCode(403);
    }

    @Test
    void enforcesEveryImplementedEmployeeCarPermission() {
        Dealer dealer = createDealer("Permission Motors");
        createUser("no-permissions@example.com", Role.DEALER_EMPLOYEE, dealer);
        createUser("add@example.com", Role.DEALER_EMPLOYEE, dealer, Permission.CAN_ADD_CAR);
        createUser("edit@example.com", Role.DEALER_EMPLOYEE, dealer, Permission.CAN_EDIT_CAR);
        createUser("delete@example.com", Role.DEALER_EMPLOYEE, dealer, Permission.CAN_DELETE_CAR);
        Car editableCar = createCar(dealer, "Honda", "Civic", 2022, "23000.00");
        Car deletableCar = createCar(dealer, "Mazda", "3", 2021, "21000.00");

        Map<String, Object> newCar = Map.of(
                "make", "Toyota", "model", "Camry", "modelYear", 2024, "price", 32000);

        authorized(login("no-permissions@example.com"))
                .queryParam("dealerId", dealer.getId())
                .body(newCar)
                .post("/api/v1/cars")
                .then()
                .statusCode(403);

        authorized(login("add@example.com"))
                .queryParam("dealerId", dealer.getId())
                .body(newCar)
                .post("/api/v1/cars")
                .then()
                .statusCode(201);

        Map<String, Object> update = Map.of(
                "make", "Honda", "model", "Civic Touring", "modelYear", 2023, "price", 26000);

        authorized(login("no-permissions@example.com"))
                .body(update)
                .put("/api/v1/cars/{id}", editableCar.getId())
                .then()
                .statusCode(403);

        authorized(login("edit@example.com"))
                .body(update)
                .put("/api/v1/cars/{id}", editableCar.getId())
                .then()
                .statusCode(200)
                .body("model", equalTo("Civic Touring"));

        authorized(login("no-permissions@example.com"))
                .delete("/api/v1/cars/{id}", deletableCar.getId())
                .then()
                .statusCode(403);

        authorized(login("delete@example.com"))
                .delete("/api/v1/cars/{id}", deletableCar.getId())
                .then()
                .statusCode(200);
    }

    @Test
    void isolatesDealerDataAndRejectsCrossTenantMutations() {
        Dealer dealerA = createDealer("Tenant A Motors");
        Dealer dealerB = createDealer("Tenant B Motors");
        createUser("admin-a@example.com", Role.DEALER_ADMIN, dealerA);
        Car carA = createCar(dealerA, "Toyota", "Corolla", 2022, "22000.00");
        Car carB = createCar(dealerB, "Ford", "Escape", 2023, "31000.00");
        String token = login("admin-a@example.com");

        authorized(token)
                .get("/api/v1/cars")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content.id", containsInAnyOrder(carA.getId().intValue()));

        authorized(token)
                .get("/api/v1/cars/{id}", carB.getId())
                .then()
                .statusCode(404);

        authorized(token)
                .get("/api/v1/dealers/{id}", dealerB.getId())
                .then()
                .statusCode(403);

        authorized(token)
                .queryParam("dealerId", dealerB.getId())
                .body(Map.of("make", "Kia", "model", "Soul", "modelYear", 2024, "price", 25000))
                .post("/api/v1/cars")
                .then()
                .statusCode(403);

        authorized(token)
                .get("/api/v1/dealers/{id}/employees", dealerB.getId())
                .then()
                .statusCode(403);
    }

    @Test
    void returnsConflictForDuplicateEmployeeEmail() {
        Dealer dealer = createDealer("Conflict Motors");
        createUser("dealer-admin@example.com", Role.DEALER_ADMIN, dealer);
        String token = login("dealer-admin@example.com");
        Map<String, Object> employee = Map.of(
                "email", "duplicate@example.com",
                "password", TEST_PASSWORD,
                "permissions", new String[] {"CAN_ADD_CAR"});

        authorized(token)
                .body(employee)
                .post("/api/v1/dealers/{dealerId}/employees", dealer.getId())
                .then()
                .statusCode(201);

        authorized(token)
                .body(employee)
                .post("/api/v1/dealers/{dealerId}/employees", dealer.getId())
                .then()
                .statusCode(409)
                .body("status", equalTo(409))
                .body("error", equalTo("Conflict"));
    }
}
