package ca.sheridancollege.munjalru.controllers;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.dto.CarResponse;
import ca.sheridancollege.munjalru.dto.DealerRequest;
import ca.sheridancollege.munjalru.dto.DealerResponse;
import ca.sheridancollege.munjalru.models.AuthenticationRequest;
import ca.sheridancollege.munjalru.services.AuthenticationService;
import ca.sheridancollege.munjalru.services.CarService;
import ca.sheridancollege.munjalru.services.DealerDashboardService;
import ca.sheridancollege.munjalru.services.DealerManagementService;
import ca.sheridancollege.munjalru.services.DealerService;
import io.micrometer.core.instrument.simple.SimpleMeterRegistry;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.BadCredentialsException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ControllerMetricsTest {

    private SimpleMeterRegistry meterRegistry;

    @BeforeEach
    void setUp() {
        meterRegistry = new SimpleMeterRegistry();
    }

    @Test
    void failedAuthenticationIncrementsFailureCounter() {
        AuthenticationService authenticationService = mock(AuthenticationService.class);
        AuthenticationController controller =
                new AuthenticationController(authenticationService, meterRegistry);
        AuthenticationRequest request = new AuthenticationRequest();
        when(authenticationService.authenticate(request))
                .thenThrow(new BadCredentialsException("Invalid credentials"));

        assertThatThrownBy(() -> controller.authenticate(request))
                .isInstanceOf(BadCredentialsException.class);

        assertThat(meterRegistry.get("dealership.auth.failures").counter().count())
                .isEqualTo(1.0);
    }

    @Test
    void successfulCarMutationsIncrementOperationCounters() {
        CarService carService = mock(CarService.class);
        CarRestController controller = new CarRestController(carService, meterRegistry);
        CarRequest request = new CarRequest();
        User siteAdmin = siteAdmin();
        when(carService.createForDealer(1L, request)).thenReturn(new CarResponse());
        when(carService.update(1L, request)).thenReturn(new CarResponse());

        controller.addCar(request, 1L, siteAdmin);
        controller.updateCar(1L, request, siteAdmin);
        controller.deleteCar(1L, siteAdmin);

        assertMutationCounts("dealership.car.mutations");
    }

    @Test
    void failedCarMutationDoesNotIncrementCounter() {
        CarService carService = mock(CarService.class);
        CarRestController controller = new CarRestController(carService, meterRegistry);
        CarRequest request = new CarRequest();
        doThrow(new IllegalStateException("Package limit reached"))
                .when(carService).delete(1L);

        assertThatThrownBy(() -> controller.deleteCar(1L, siteAdmin()))
                .isInstanceOf(IllegalStateException.class);

        assertThat(meterRegistry.find("dealership.car.mutations").counter()).isNull();
    }

    @Test
    void successfulDealerMutationsIncrementOperationCounters() {
        DealerService dealerService = mock(DealerService.class);
        DealerRestController controller = new DealerRestController(
                dealerService,
                mock(DealerManagementService.class),
                mock(DealerDashboardService.class),
                meterRegistry);
        DealerRequest request = new DealerRequest();
        when(dealerService.create(request)).thenReturn(new DealerResponse());
        when(dealerService.update(1L, request)).thenReturn(new DealerResponse());

        controller.addDealer(request);
        controller.updateDealer(1L, request, siteAdmin());
        controller.deleteDealer(1L);

        assertMutationCounts("dealership.dealer.mutations");
    }

    private void assertMutationCounts(String metricName) {
        assertThat(meterRegistry.get(metricName).tag("operation", "create").counter().count())
                .isEqualTo(1.0);
        assertThat(meterRegistry.get(metricName).tag("operation", "update").counter().count())
                .isEqualTo(1.0);
        assertThat(meterRegistry.get(metricName).tag("operation", "delete").counter().count())
                .isEqualTo(1.0);
    }

    private static User siteAdmin() {
        User user = new User();
        user.setRole(Role.SITE_ADMIN);
        return user;
    }
}
