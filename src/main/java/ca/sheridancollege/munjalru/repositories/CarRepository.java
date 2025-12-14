package ca.sheridancollege.munjalru.repositories;

import ca.sheridancollege.munjalru.beans.Car;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CarRepository extends JpaRepository<Car, Long> {
}
