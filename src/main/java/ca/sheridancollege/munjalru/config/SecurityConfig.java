package ca.sheridancollege.munjalru.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpHeaders;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.factory.PasswordEncoderFactories;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private static final String LOCAL_PROFILE = "local";

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final AuthenticationProvider authenticationProvider;
    private final Environment environment;
    private final RateLimitFilter rateLimitFilter;

    @Bean
    @Order(1)
    public SecurityFilterChain prometheusSecurityFilterChain(
            HttpSecurity http,
            @Value("${app.observability.prometheus.password}") String prometheusPassword) throws Exception {
        PasswordEncoder passwordEncoder = PasswordEncoderFactories.createDelegatingPasswordEncoder();
        UserDetails prometheusUser = User.withUsername("prometheus")
                .password(passwordEncoder.encode(prometheusPassword))
                .roles("PROMETHEUS")
                .build();
        DaoAuthenticationProvider prometheusAuthenticationProvider =
                new DaoAuthenticationProvider(new InMemoryUserDetailsManager(prometheusUser));
        prometheusAuthenticationProvider.setPasswordEncoder(passwordEncoder);
        AuthenticationEntryPoint prometheusAuthenticationEntryPoint = (request, response, exception) -> {
            response.setHeader(HttpHeaders.WWW_AUTHENTICATE, "Basic realm=\"prometheus\"");
            response.setStatus(401);
        };

        return http
                .securityMatcher("/actuator/prometheus")
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> authorize.anyRequest().authenticated())
                .authenticationProvider(prometheusAuthenticationProvider)
                .httpBasic(httpBasic -> httpBasic.authenticationEntryPoint(prometheusAuthenticationEntryPoint))
                .exceptionHandling(exceptionHandling ->
                        exceptionHandling.authenticationEntryPoint(prometheusAuthenticationEntryPoint))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .build();
    }

    @Bean
    @Order(2)
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        boolean isLocal = isLocalProfileActive();

        if (isLocal) {
            http.headers(headers -> headers.frameOptions(frameOptions -> frameOptions.disable()));
        }

        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorize -> {
                    authorize
                            .requestMatchers("/actuator/health").permitAll()
                            .requestMatchers("/api/v1/auth/**").permitAll()
                            .requestMatchers("/", "/index.html", "/login", "/register").permitAll()
                            .requestMatchers("/*.js", "/*.css", "/*.ico", "/*.html").permitAll()
                            .requestMatchers("/assets/**", "/static/**").permitAll()
                            .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll();

                    if (isLocal) {
                        authorize.requestMatchers("/h2-console/**").permitAll();
                    }

                    authorize.anyRequest().authenticated();
                })
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .addFilterBefore(rateLimitFilter, JwtAuthenticationFilter.class)
                .build();
    }

    @Value("${app.cors.allowed-origins:http://localhost:4200,http://localhost:5000,http://localhost:6001}")
    private List<String> allowedOrigins;

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(allowedOrigins);
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }

    private boolean isLocalProfileActive() {
        return Arrays.asList(environment.getActiveProfiles()).contains(LOCAL_PROFILE);
    }
}
