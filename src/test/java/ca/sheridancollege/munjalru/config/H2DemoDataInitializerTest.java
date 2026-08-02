package ca.sheridancollege.munjalru.config;

import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Permission;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;

import java.util.Set;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

@SpringBootTest(properties = {
        "JWT_SECRET=DdHcnCpz5Ofm9QiSbEopDNO1yuE2jfPSVdPcJPdWcdhrQ4/rZhgSHDgIbqsw/X62v7qWP8FJ8ttr/D9SW3PfTw==",
        "SITE_ADMIN_EMAIL=local-siteadmin@test.local",
        "SITE_ADMIN_PASSWORD=local-admin-password",
        "DEMO_DATA_PASSWORD=IntegrationDemo123!"
})
@ActiveProfiles("local")
class H2DemoDataInitializerTest {

    @Autowired
    private H2DemoDataInitializer initializer;
    @Autowired
    private DealerRepository dealerRepository;
    @Autowired
    private CarRepository carRepository;
    @Autowired
    private PackageRepository packageRepository;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Test
    void seedsRepresentativeLocalH2DataWithoutDuplicates() {
        assertEquals(3, packageRepository.count());
        assertEquals(3, dealerRepository.count());
        assertEquals(10, carRepository.count());
        assertEquals(8, userRepository.count());

        assertTrue(dealerRepository.findAll().stream()
                .anyMatch(dealer -> dealer.getStatus() == DealerStatus.SUSPENDED));
        assertTrue(dealerRepository.findAll().stream()
                .allMatch(dealer -> dealer.getDealerPackage() != null));

        User inventoryEmployee = userRepository.findByEmail("maple.inventory@demo.local").orElseThrow();
        assertEquals(Role.DEALER_EMPLOYEE, inventoryEmployee.getRole());
        assertEquals(Set.of(Permission.values()), inventoryEmployee.getPermissions());
        assertTrue(passwordEncoder.matches("IntegrationDemo123!", inventoryEmployee.getPassword()));

        User inactiveEmployee = userRepository.findByEmail("harbour.inventory@demo.local").orElseThrow();
        assertFalse(inactiveEmployee.isActive());

        initializer.run();

        assertEquals(3, dealerRepository.count());
        assertEquals(10, carRepository.count());
        assertEquals(8, userRepository.count());
    }

    @Test
    void isRestrictedToTheLocalH2Environment() {
        Profile profile = H2DemoDataInitializer.class.getAnnotation(Profile.class);
        ConditionalOnProperty condition = H2DemoDataInitializer.class.getAnnotation(ConditionalOnProperty.class);

        assertNotNull(profile);
        assertArrayEquals(new String[]{"local"}, profile.value());
        assertNotNull(condition);
        assertArrayEquals(new String[]{"spring.datasource.driver-class-name"}, condition.name());
        assertEquals("org.h2.Driver", condition.havingValue());
        assertFalse(condition.matchIfMissing());
    }
}
