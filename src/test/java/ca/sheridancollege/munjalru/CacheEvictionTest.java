package ca.sheridancollege.munjalru;

import ca.sheridancollege.munjalru.beans.Dealer;
import ca.sheridancollege.munjalru.beans.DealerStatus;
import ca.sheridancollege.munjalru.beans.Package;
import ca.sheridancollege.munjalru.config.CacheConfig;
import ca.sheridancollege.munjalru.dto.CarRequest;
import ca.sheridancollege.munjalru.repositories.CarRepository;
import ca.sheridancollege.munjalru.repositories.DealerRepository;
import ca.sheridancollege.munjalru.repositories.PackageRepository;
import ca.sheridancollege.munjalru.repositories.UserRepository;
import ca.sheridancollege.munjalru.services.CarService;
import ca.sheridancollege.munjalru.services.DealerDashboardService;
import ca.sheridancollege.munjalru.services.DealerManagementService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

@SpringBootTest
class CacheEvictionTest extends IntegrationTestBase {

    @Autowired private DealerDashboardService dashboardService;
    @Autowired private DealerManagementService dealerManagementService;
    @Autowired private CarService carService;
    @Autowired private CacheManager cacheManager;
    @Autowired private CarRepository carRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private DealerRepository dealerRepository;
    @Autowired private PackageRepository packageRepository;

    private Dealer dealer;
    private Package basicPackage;

    @BeforeEach
    void setUp() {
        cache().clear();
        carRepository.deleteAll();
        userRepository.deleteAll();
        dealerRepository.deleteAll();
        packageRepository.deleteAll();

        basicPackage = packageRepository.save(Package.builder()
                .name("Basic Cache Test").maxEmployeeSeats(5).maxCarListings(20).build());
        dealer = dealerRepository.save(Dealer.builder()
                .name("Cache Motors").location("Toronto")
                .status(DealerStatus.ACTIVE).visible(true).dealerPackage(basicPackage).build());
    }

    @Test
    void addingCarEvictsAndRebuildsDashboardSummary() {
        assertEquals(0, dashboardService.getSummary(dealer.getId()).getCarCount());
        assertNotNull(cache().get(dealer.getId()));

        carService.createForDealer(dealer.getId(), CarRequest.builder()
                .make("Toyota").model("Camry").modelYear(2025).build());

        assertNull(cache().get(dealer.getId()));
        assertEquals(1, dashboardService.getSummary(dealer.getId()).getCarCount());
    }

    @Test
    void assigningPackageEvictsCachedLimits() {
        assertEquals(5, dashboardService.getSummary(dealer.getId()).getMaxEmployeeSeats());
        Package proPackage = packageRepository.save(Package.builder()
                .name("Pro Cache Test").maxEmployeeSeats(15).maxCarListings(100).build());

        dealerManagementService.assignPackage(dealer.getId(), proPackage.getId());

        assertNull(cache().get(dealer.getId()));
        assertEquals(15, dashboardService.getSummary(dealer.getId()).getMaxEmployeeSeats());
    }

    private Cache cache() {
        Cache cache = cacheManager.getCache(CacheConfig.DEALER_DASHBOARD_CACHE);
        assertNotNull(cache);
        return cache;
    }
}
