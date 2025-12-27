package ca.sheridancollege.munjalru.repositories;

import ca.sheridancollege.munjalru.beans.Package;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PackageRepository extends JpaRepository<Package, Long> {
    Optional<Package> findByName(String name);
}
