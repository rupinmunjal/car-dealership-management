package ca.sheridancollege.munjalru.config;

import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps initial platform data:
 * 1. Default subscription packages (Basic, Pro, Enterprise)
 * 2. The initial {@link Role#SITE_ADMIN} account
 */
@Slf4j
@Component
@Order(0)
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PackageRepository packageRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SITE_ADMIN_EMAIL:}")
    private String siteAdminEmail;

    @Value("${SITE_ADMIN_PASSWORD:}")
    private String siteAdminPassword;

    public DataInitializer(UserRepository userRepository,
                           PackageRepository packageRepository,
                           PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.packageRepository = packageRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
        bootstrapPackages();
        bootstrapSiteAdmin();
    }

    private void bootstrapPackages() {
        if (packageRepository.count() == 0) {
            packageRepository.save(Package.builder().name("Basic").maxEmployeeSeats(5).maxCarListings(25).build());
            packageRepository.save(Package.builder().name("Pro").maxEmployeeSeats(15).maxCarListings(100).build());
            packageRepository.save(Package.builder().name("Enterprise").maxEmployeeSeats(50).maxCarListings(500).build());
            log.info("Bootstrapped default packages: Basic (5/25), Pro (15/100), Enterprise (50/500)");
        }
    }

    private void bootstrapSiteAdmin() {
        boolean siteAdminExists = userRepository.findAll().stream()
                .anyMatch(u -> u.getRole() == Role.SITE_ADMIN);

        if (siteAdminExists) {
            log.info("SITE_ADMIN already exists — skipping bootstrap.");
            return;
        }

        if (siteAdminEmail.isBlank() || siteAdminPassword.isBlank()) {
            throw new IllegalStateException(
                    "No SITE_ADMIN account exists and SITE_ADMIN_EMAIL / SITE_ADMIN_PASSWORD "
                            + "environment variables are not set. Set both and restart the application "
                            + "to bootstrap the initial platform admin account."
            );
        }

        User admin = User.builder()
                .email(siteAdminEmail)
                .password(passwordEncoder.encode(siteAdminPassword))
                .role(Role.SITE_ADMIN)
                .build();
        userRepository.save(admin);
        log.info("Bootstrapped SITE_ADMIN account: {}", siteAdminEmail);
    }
}
