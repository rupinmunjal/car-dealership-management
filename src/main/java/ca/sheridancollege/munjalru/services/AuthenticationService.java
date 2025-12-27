package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.models.AuthenticationRequest;
import ca.sheridancollege.munjalru.models.AuthenticationResponse;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import lombok.AllArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
@AllArgsConstructor
public class AuthenticationService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    /**
     * Registers a new user with the {@link Role#DEALER_EMPLOYEE} role.
     * No dealer is assigned during public registration — that is done
     * later by a DEALER_ADMIN via a separate endpoint.
     */
    public AuthenticationResponse register(AuthenticationRequest request) {
        User user = User.builder()
                .email(request.getEmail())
                .password(passwordEncoder.encode(request.getPassword()))
                .role(Role.DEALER_EMPLOYEE)
                .active(true)
                .dealerStatus(DealerStatus.ACTIVE)
                .build();
        userRepository.save(user);
        var jwtToken = jwtService.generateToken(user);
        return AuthenticationResponse.builder()
                .token(jwtToken)
                .role(user.getRole().name())
                .dealerId(null)
                .permissions(Collections.emptyList())
                .build();
    }

    /**
     * Authenticates an existing user and returns a JWT with embedded
     * role, dealerId, dealerStatus, and permissions claims.
     */
    public AuthenticationResponse authenticate(AuthenticationRequest request) {
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.getEmail(), request.getPassword())
        );
        User user = userRepository.findByEmail(request.getEmail()).orElseThrow();

        // Populate the transient dealerStatus from the DB so JwtService embeds it
        if (user.getDealer() != null) {
            user.setDealerStatus(user.getDealer().getStatus());
        }

        var jwtToken = jwtService.generateToken(user);

        AuthenticationResponse.AuthenticationResponseBuilder builder =
                AuthenticationResponse.builder()
                        .token(jwtToken)
                        .role(user.getRole().name());

        if (user.getDealer() != null) {
            builder.dealerId(user.getDealer().getId());
        }

        if (user.getPermissions() != null && !user.getPermissions().isEmpty()) {
            builder.permissions(user.getPermissions().stream()
                    .map(Enum::name)
                    .toList());
        } else {
            builder.permissions(Collections.emptyList());
        }

        return builder.build();
    }
}
