package ca.sheridancollege.munjalru.config;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Bootstraps the first {@link Role#SITE_ADMIN} account on application startup.
 *
 * <p>Behaviour:
 * <ol>
 *   <li>If a SITE_ADMIN already exists → log and skip.</li>
 *   <li>If no SITE_ADMIN exists and {@code SITE_ADMIN_EMAIL} /
 *       {@code SITE_ADMIN_PASSWORD} are set → create the account.</li>
 *   <li>If no SITE_ADMIN exists and the env vars are <b>not</b> set →
 *       throw {@link IllegalStateException}, crashing startup. There is
 *       no hardcoded fallback.</li>
 * </ol>
 */
@Slf4j
@Component
public class DataInitializer implements CommandLineRunner {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Value("${SITE_ADMIN_EMAIL:}")
    private String siteAdminEmail;

    @Value("${SITE_ADMIN_PASSWORD:}")
    private String siteAdminPassword;

    public DataInitializer(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void run(String... args) {
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
