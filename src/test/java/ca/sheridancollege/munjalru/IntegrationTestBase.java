package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.Permission;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import java.util.Set;

/**
 * Base class for integration tests.
 *
 * <p>It forces the {@code test} profile and pins the datasource to an in-memory H2
 * database. This prevents a locally exported {@code SPRING_DATASOURCE_URL}
 * environment variable from overriding the test database URL.</p>
 *
 * <p>Also provides shared authentication helpers that construct a custom
 * {@link User} principal — needed because {@code @WithMockUser} creates a
 * Spring Security {@code User}, not our domain {@code User}, so
 * {@code @AuthenticationPrincipal} would resolve to {@code null}.</p>
 */
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1;DB_CLOSE_ON_EXIT=FALSE"
})
public abstract class IntegrationTestBase {

    /** A SITE_ADMIN principal with no dealer affiliation. */
    protected static UsernamePasswordAuthenticationToken siteAdminAuth() {
        User user = new User();
        user.setId(1L);
        user.setEmail("admin@site.com");
        user.setRole(Role.SITE_ADMIN);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    /** A DEALER_ADMIN principal tied to the given dealer. */
    protected static UsernamePasswordAuthenticationToken dealerAdminAuth(Dealer dealer) {
        User user = new User();
        user.setId(2L);
        user.setEmail("dadmin@test.com");
        user.setRole(Role.DEALER_ADMIN);
        user.setDealer(dealer);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }

    /** A DEALER_EMPLOYEE principal tied to the given dealer with the given permissions. */
    protected static UsernamePasswordAuthenticationToken dealerEmployeeAuth(Dealer dealer, Set<Permission> perms) {
        User user = new User();
        user.setId(3L);
        user.setEmail("emp@test.com");
        user.setRole(Role.DEALER_EMPLOYEE);
        user.setDealer(dealer);
        user.setPermissions(perms);
        return new UsernamePasswordAuthenticationToken(user, null, user.getAuthorities());
    }
}
