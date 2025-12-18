package ca.sheridancollege.munjalru.repositories;

import ca.sheridancollege.munjalru.beans.Dealer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface DealerRepository extends JpaRepository<Dealer, Long> {

    /** Find the dealer that owns a given car by the car's ID. */
    Optional<Dealer> findByCarsId(Long carId);
}
