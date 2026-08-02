package ca.sheridancollege.munjalru.repositories;

import ca.sheridancollege.munjalru.beans.Car;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;

public interface CarRepository extends JpaRepository<Car, Long> {

    @Query("""
            select car from Car car
            where (:make is null or lower(car.make) like lower(concat('%', cast(:make as string), '%')))
              and (:model is null or lower(car.model) like lower(concat('%', cast(:model as string), '%')))
              and (:minPrice is null or car.price >= :minPrice)
              and (:maxPrice is null or car.price <= :maxPrice)
              and (:year is null or car.modelYear = :year)
              and (:search is null or lower(car.make) like lower(concat('%', cast(:search as string), '%'))
                   or lower(car.model) like lower(concat('%', cast(:search as string), '%')))
            """)
    Page<Car> findAllFiltered(
            @Param("make") String make,
            @Param("model") String model,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("year") Integer year,
            @Param("search") String search,
            Pageable pageable);

    @Query("""
            select car from Dealer dealer join dealer.cars car
            where dealer.id = :dealerId
              and (:make is null or lower(car.make) like lower(concat('%', cast(:make as string), '%')))
              and (:model is null or lower(car.model) like lower(concat('%', cast(:model as string), '%')))
              and (:minPrice is null or car.price >= :minPrice)
              and (:maxPrice is null or car.price <= :maxPrice)
              and (:year is null or car.modelYear = :year)
              and (:search is null or lower(car.make) like lower(concat('%', cast(:search as string), '%'))
                   or lower(car.model) like lower(concat('%', cast(:search as string), '%')))
            """)
    Page<Car> findAllFilteredByDealer(
            @Param("dealerId") Long dealerId,
            @Param("make") String make,
            @Param("model") String model,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            @Param("year") Integer year,
            @Param("search") String search,
            Pageable pageable);
}
