package ca.sheridancollege.munjalru.api;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Permission;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import io.restassured.RestAssured;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import org.junit.jupiter.api.BeforeEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.connection.RedisConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.lifecycle.Startables;
import org.testcontainers.utility.DockerImageName;

import javax.crypto.SecretKey;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.Date;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

@ActiveProfiles("api-it")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
abstract class RestAssuredIntegrationTestBase {

    protected static final String JWT_SECRET =
            "DdHcnCpz5Ofm9QiSbEopDNO1yuE2jfPSVdPcJPdWcdhrQ4/rZhgSHDgIbqsw/X62v7qWP8FJ8ttr/D9SW3PfTw==";
    protected static final String TEST_PASSWORD = "Password123!";

    static final PostgreSQLContainer<?> POSTGRES = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("dealership_api_it")
            .withUsername("api_it")
            .withPassword("api_it_password");

    static final GenericContainer<?> REDIS = new GenericContainer<>(DockerImageName.parse("redis:7-alpine"))
            .withExposedPorts(6379);

    static {
        Startables.deepStart(Stream.of(POSTGRES, REDIS)).join();
    }

    @DynamicPropertySource
    static void containerProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", () -> "org.postgresql.Driver");
        registry.add("spring.data.redis.host", REDIS::getHost);
        registry.add("spring.data.redis.port", () -> REDIS.getMappedPort(6379));
    }

    @LocalServerPort
    private int port;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Autowired
    private RedisConnectionFactory redisConnectionFactory;

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Autowired
    protected UserRepository userRepository;

    @Autowired
    protected DealerRepository dealerRepository;

    @Autowired
    protected CarRepository carRepository;

    @Autowired
    protected PackageRepository packageRepository;

    private RequestSpecification requestSpecification;

    @BeforeEach
    void resetIntegrationState() {
        jdbcTemplate.execute("TRUNCATE TABLE audit_log, user_permissions, _user, car, dealer, "
                + "dealer_package RESTART IDENTITY CASCADE");
        try (RedisConnection connection = redisConnectionFactory.getConnection()) {
            connection.serverCommands().flushAll();
        }

        RestAssured.enableLoggingOfRequestAndResponseIfValidationFails();
        requestSpecification = new RequestSpecBuilder()
                .setBaseUri("http://127.0.0.1")
                .setPort(port)
                .setContentType(ContentType.JSON)
                .setAccept(ContentType.JSON)
                .build();
    }

    protected RequestSpecification request() {
        return RestAssured.given().spec(requestSpecification);
    }

    protected RequestSpecification authorized(String token) {
        return request().header("Authorization", "Bearer " + token);
    }

    protected String login(String email) {
        return login(email, TEST_PASSWORD);
    }

    protected String login(String email, String password) {
        return request()
                .body(Map.of("email", email, "password", password))
                .post("/api/v1/auth/login")
                .then()
                .statusCode(200)
                .extract()
                .path("token");
    }

    protected ca.sheridancollege.munjalru.beans.Package createPackage(
            String name, int employeeSeats, int carListings) {
        return packageRepository.saveAndFlush(ca.sheridancollege.munjalru.beans.Package.builder()
                .name(name)
                .maxEmployeeSeats(employeeSeats)
                .maxCarListings(carListings)
                .build());
    }

    protected Dealer createDealer(String name) {
        return createDealer(name, createPackage(name + " Plan", 20, 100));
    }

    protected Dealer createDealer(String name, ca.sheridancollege.munjalru.beans.Package dealerPackage) {
        return dealerRepository.saveAndFlush(Dealer.builder()
                .name(name)
                .location("Toronto")
                .status(DealerStatus.ACTIVE)
                .dealerPackage(dealerPackage)
                .build());
    }

    protected User createUser(String email, Role role, Dealer dealer, Permission... permissions) {
        return userRepository.saveAndFlush(User.builder()
                .email(email)
                .password(passwordEncoder.encode(TEST_PASSWORD))
                .role(role)
                .dealer(dealer)
                .permissions(Set.of(permissions))
                .active(true)
                .dealerStatus(dealer == null ? DealerStatus.ACTIVE : dealer.getStatus())
                .build());
    }

    protected Car createCar(Dealer dealer, String make, String model, int year, String price) {
        Car car = carRepository.saveAndFlush(Car.builder()
                .make(make)
                .model(model)
                .modelYear(year)
                .price(new BigDecimal(price))
                .build());
        dealer.getCars().add(car);
        dealerRepository.saveAndFlush(dealer);
        return car;
    }

    protected String expiredToken(String subject, Role role) {
        return signedToken(subject, role, JWT_SECRET, Instant.now().minusSeconds(120), Instant.now().minusSeconds(60));
    }

    protected String tokenWithInvalidSignature(String subject, Role role) {
        String otherSecret = "VGVzdEludGVncmF0aW9uU2VjcmV0VGhhdElzTG9uZ0Vub3VnaEZvckhTMjU2U2lnbmluZ0tleTEyMzQ1Njc4OTA=";
        return signedToken(subject, role, otherSecret, Instant.now(), Instant.now().plusSeconds(60));
    }

    private String signedToken(String subject, Role role, String secret, Instant issuedAt, Instant expiration) {
        SecretKey key = Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
        return Jwts.builder()
                .claim("role", role.name())
                .subject(subject)
                .issuedAt(Date.from(issuedAt))
                .expiration(Date.from(expiration))
                .signWith(key)
                .compact();
    }
}
