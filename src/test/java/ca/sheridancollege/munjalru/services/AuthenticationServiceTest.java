package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.models.AuthenticationRequest;
import ca.sheridancollege.munjalru.models.AuthenticationResponse;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthenticationServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private JwtService jwtService;

    @InjectMocks
    private AuthenticationService authenticationService;

    @Test
    void shouldRegisterUser() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        User savedUser = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .role(Role.DEALER_EMPLOYEE)
                .build();

        when(passwordEncoder.encode(anyString())).thenReturn("encoded-password");
        when(userRepository.save(any(User.class))).thenReturn(savedUser);
        when(jwtService.generateToken(any(User.class))).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.register(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("DEALER_EMPLOYEE");
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void shouldAuthenticateUser() {
        AuthenticationRequest request = AuthenticationRequest.builder()
                .email("user@example.com")
                .password("password123")
                .build();

        User user = User.builder()
                .id(1L)
                .email("user@example.com")
                .password("encoded-password")
                .role(Role.SITE_ADMIN)
                .build();

        when(userRepository.findByEmail("user@example.com")).thenReturn(Optional.of(user));
        when(jwtService.generateToken(user)).thenReturn("jwt-token");

        AuthenticationResponse response = authenticationService.authenticate(request);

        assertThat(response.getToken()).isEqualTo("jwt-token");
        assertThat(response.getRole()).isEqualTo("SITE_ADMIN");
        verify(authenticationManager, times(1))
                .authenticate(any(UsernamePasswordAuthenticationToken.class));
    }
}
