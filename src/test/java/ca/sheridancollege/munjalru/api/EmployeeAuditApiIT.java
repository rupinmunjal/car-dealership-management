package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Permission;
import ca.sheridancollege.munjalru.beans.Role;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasSize;

class EmployeeAuditApiIT extends RestAssuredIntegrationTestBase {

    @Test
    void managesEmployeeLifecycleWithPaginationAndPermissions() {
        Dealer dealer = createDealer("Employee Motors");
        createUser("dealer-admin@example.com", Role.DEALER_ADMIN, dealer);
        String token = login("dealer-admin@example.com");

        long employeeId = authorized(token)
                .body(Map.of(
                        "email", "employee@example.com",
                        "password", TEST_PASSWORD,
                        "permissions", new String[] {"CAN_ADD_CAR"}))
                .post("/api/v1/dealers/{dealerId}/employees", dealer.getId())
                .then()
                .statusCode(201)
                .body("role", equalTo("DEALER_EMPLOYEE"))
                .body("permissions", containsInAnyOrder("CAN_ADD_CAR"))
                .extract().jsonPath().getLong("id");

        authorized(token)
                .queryParam("page", 0)
                .queryParam("size", 1)
                .get("/api/v1/dealers/{dealerId}/employees", dealer.getId())
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].email", equalTo("employee@example.com"))
                .body("totalElements", equalTo(1));

        authorized(token)
                .body(Map.of("permissions", new String[] {"CAN_EDIT_CAR", "CAN_DELETE_CAR"}))
                .put("/api/v1/dealers/{dealerId}/employees/{employeeId}/permissions",
                        dealer.getId(), employeeId)
                .then()
                .statusCode(200)
                .body("permissions", containsInAnyOrder("CAN_EDIT_CAR", "CAN_DELETE_CAR"));

        authorized(token)
                .delete("/api/v1/dealers/{dealerId}/employees/{employeeId}",
                        dealer.getId(), employeeId)
                .then()
                .statusCode(200);

        authorized(token)
                .get("/api/v1/dealers/{dealerId}/employees", dealer.getId())
                .then()
                .statusCode(200)
                .body("content", hasSize(0));

        request()
                .body(Map.of("email", "employee@example.com", "password", TEST_PASSWORD))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(401);
    }

    @Test
    void validatesEmployeeRequestsAndReturnsNotFound() {
        Dealer dealer = createDealer("Employee Validation Motors");
        createUser("dealer-admin@example.com", Role.DEALER_ADMIN, dealer);
        String token = login("dealer-admin@example.com");

        authorized(token)
                .body(Map.of("email", "invalid", "password", ""))
                .post("/api/v1/dealers/{dealerId}/employees", dealer.getId())
                .then()
                .statusCode(400)
                .body("error", equalTo("Validation Failed"));

        authorized(token)
                .body(Map.of("permissions", new String[] {"CAN_ADD_CAR"}))
                .put("/api/v1/dealers/{dealerId}/employees/{employeeId}/permissions",
                        dealer.getId(), 999999)
                .then()
                .statusCode(404)
                .body("error", equalTo("Not Found"));
    }

    @Test
    void scopesAuditLogsByDealerWhileSiteAdminSeesAll() {
        Dealer dealerA = createDealer("Audit A Motors");
        Dealer dealerB = createDealer("Audit B Motors");
        createUser("site-admin@example.com", Role.SITE_ADMIN, null);
        createUser("admin-a@example.com", Role.DEALER_ADMIN, dealerA);
        createUser("admin-b@example.com", Role.DEALER_ADMIN, dealerB);

        createEmployeeThroughApi(dealerA, "admin-a@example.com", "employee-a@example.com");
        createEmployeeThroughApi(dealerB, "admin-b@example.com", "employee-b@example.com");

        authorized(login("admin-a@example.com"))
                .get("/api/v1/audit-logs")
                .then()
                .statusCode(200)
                .body("content", hasSize(1))
                .body("content[0].dealerId", equalTo(dealerA.getId().intValue()))
                .body("content[0].action", equalTo("EMPLOYEE_HIRED"));

        authorized(login("site-admin@example.com"))
                .queryParam("size", 10)
                .get("/api/v1/audit-logs")
                .then()
                .statusCode(200)
                .body("content", hasSize(2))
                .body("content.action", containsInAnyOrder("EMPLOYEE_HIRED", "EMPLOYEE_HIRED"));
    }

    private void createEmployeeThroughApi(Dealer dealer, String adminEmail, String employeeEmail) {
        authorized(login(adminEmail))
                .body(Map.of(
                        "email", employeeEmail,
                        "password", TEST_PASSWORD,
                        "permissions", new String[] {Permission.CAN_ADD_CAR.name()}))
                .post("/api/v1/dealers/{dealerId}/employees", dealer.getId())
                .then()
                .statusCode(201);
    }
}
