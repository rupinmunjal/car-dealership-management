package ca.sheridancollege.munjalru.config;

import ca.sheridancollege.munjalru.beans.Car;
import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.beans.Permission;
import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Profile;
import org.springframework.core.annotation.Order;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

/** Seeds representative demo records only for the local H2 environment. */
@Slf4j
@Component
@Profile("local")
@Order(1)
@ConditionalOnProperty(
        name = "spring.datasource.driver-class-name",
        havingValue = "org.h2.Driver"
)
public class H2DemoDataInitializer implements CommandLineRunner {

    private final DealerRepository dealerRepository;
    private final PackageRepository packageRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final String demoPassword;

    public H2DemoDataInitializer(DealerRepository dealerRepository,
                                 PackageRepository packageRepository,
                                 UserRepository userRepository,
                                 PasswordEncoder passwordEncoder,
                                 @Value("${app.demo-data.password:Demo123!}") String demoPassword) {
        this.dealerRepository = dealerRepository;
        this.packageRepository = packageRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.demoPassword = demoPassword;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (dealerRepository.count() > 0) {
            log.info("Dealers already exist; skipping local H2 demo data bootstrap.");
            return;
        }

        Package basic = requirePackage("Basic");
        Package pro = requirePackage("Pro");
        Package enterprise = requirePackage("Enterprise");

        Dealer maple = saveDealer(
                "Maple Auto Group",
                "Toronto, ON",
                "Maple Auto Group",
                "High-volume family vehicles and electric inventory.",
                DealerStatus.ACTIVE,
                pro,
                List.of(
                        car("Toyota", "Camry", 2024),
                        car("Honda", "CR-V", 2023),
                        car("Tesla", "Model 3", 2025),
                        car("Ford", "F-150", 2022)
                )
        );

        Dealer lakeshore = saveDealer(
                "Lakeshore Motors",
                "Oakville, ON",
                "Lakeshore Motors Oakville",
                "A compact local dealership with practical daily drivers.",
                DealerStatus.ACTIVE,
                basic,
                List.of(
                        car("Mazda", "CX-5", 2024),
                        car("Hyundai", "Elantra", 2023),
                        car("Subaru", "Outback", 2022)
                )
        );

        Dealer harbour = saveDealer(
                "Harbour Luxury Auto",
                "Mississauga, ON",
                "Harbour Luxury Auto",
                "Premium and performance vehicles for the GTA.",
                DealerStatus.SUSPENDED,
                enterprise,
                List.of(
                        car("BMW", "X5", 2024),
                        car("Mercedes-Benz", "C 300", 2023),
                        car("Audi", "Q7", 2025)
                )
        );

        String encodedPassword = passwordEncoder.encode(demoPassword);
        userRepository.saveAll(List.of(
                user("maple.admin@demo.local", encodedPassword, Role.DEALER_ADMIN, maple, Set.of(), true),
                user("maple.inventory@demo.local", encodedPassword, Role.DEALER_EMPLOYEE, maple,
                        Set.of(Permission.CAN_ADD_CAR, Permission.CAN_EDIT_CAR, Permission.CAN_DELETE_CAR), true),
                user("maple.sales@demo.local", encodedPassword, Role.DEALER_EMPLOYEE, maple,
                        Set.of(Permission.CAN_ADD_CAR, Permission.CAN_EDIT_CAR), true),
                user("lakeshore.admin@demo.local", encodedPassword, Role.DEALER_ADMIN, lakeshore, Set.of(), true),
                user("lakeshore.sales@demo.local", encodedPassword, Role.DEALER_EMPLOYEE, lakeshore,
                        Set.of(Permission.CAN_ADD_CAR), true),
                user("harbour.admin@demo.local", encodedPassword, Role.DEALER_ADMIN, harbour, Set.of(), true),
                user("harbour.inventory@demo.local", encodedPassword, Role.DEALER_EMPLOYEE, harbour,
                        Set.of(Permission.CAN_ADD_CAR, Permission.CAN_EDIT_CAR, Permission.CAN_DELETE_CAR), false)
        ));

        log.info("Bootstrapped local H2 demo data: 3 dealers, 10 cars, and 7 dealer accounts.");
    }

    private Package requirePackage(String name) {
        return packageRepository.findByName(name)
                .orElseThrow(() -> new IllegalStateException("Required demo package not found: " + name));
    }

    private Dealer saveDealer(String name,
                              String location,
                              String displayName,
                              String description,
                              DealerStatus status,
                              Package dealerPackage,
                              List<Car> cars) {
        return dealerRepository.save(Dealer.builder()
                .name(name)
                .location(location)
                .displayName(displayName)
                .description(description)
                .status(status)
                .visible(true)
                .dealerPackage(dealerPackage)
                .cars(new ArrayList<>(cars))
                .build());
    }

    private Car car(String make, String model, int modelYear) {
        return Car.builder()
                .make(make)
                .model(model)
                .modelYear(modelYear)
                .build();
    }

    private User user(String email,
                      String password,
                      Role role,
                      Dealer dealer,
                      Set<Permission> permissions,
                      boolean active) {
        return User.builder()
                .email(email)
                .password(password)
                .role(role)
                .dealer(dealer)
                .permissions(permissions)
                .active(active)
                .build();
    }
}
