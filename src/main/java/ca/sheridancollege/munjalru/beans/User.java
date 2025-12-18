package ca.sheridancollege.munjalru.beans;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "_user")
public class User implements org.springframework.security.core.userdetails.UserDetails {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String email;
    private String password;

    @Enumerated(EnumType.STRING)
    private Role role;

    /**
     * The dealer this user belongs to.
     * {@code null} for {@link Role#SITE_ADMIN}; required for
     * {@link Role#DEALER_ADMIN} and {@link Role#DEALER_EMPLOYEE}.
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "dealer_id")
    private Dealer dealer;

    /**
     * Granular permissions only meaningful for {@link Role#DEALER_EMPLOYEE}.
     * {@link Role#SITE_ADMIN} and {@link Role#DEALER_ADMIN} implicitly hold
     * all permissions.
     */
    @ElementCollection(fetch = FetchType.EAGER)
    @Enumerated(EnumType.STRING)
    @CollectionTable(name = "user_permissions", joinColumns = @JoinColumn(name = "user_id"))
    @Column(name = "permission")
    @Builder.Default
    private Set<Permission> permissions = new HashSet<>();

    @Override
    public String getPassword() { return password; }
    @Override
    public String getUsername() { return email; }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        Set<GrantedAuthority> authorities = new HashSet<>();
        // Every user gets their role as a top-level authority.
        authorities.add(new SimpleGrantedAuthority(role.name()));

        // DEALER_ADMIN implicitly holds all permissions.
        // DEALER_EMPLOYEE gets only explicitly assigned permissions.
        if (role == Role.DEALER_ADMIN) {
            for (Permission p : Permission.values()) {
                authorities.add(new SimpleGrantedAuthority(p.name()));
            }
        } else if (role == Role.DEALER_EMPLOYEE && permissions != null) {
            authorities.addAll(permissions.stream()
                    .map(p -> new SimpleGrantedAuthority(p.name()))
                    .collect(Collectors.toSet()));
        }
        // SITE_ADMIN implicitly holds all permissions too.
        if (role == Role.SITE_ADMIN) {
            for (Permission p : Permission.values()) {
                authorities.add(new SimpleGrantedAuthority(p.name()));
            }
        }
        return authorities;
    }
}
