package ca.sheridancollege.munjalru.repositories;

import ca.sheridancollege.munjalru.beans.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
}
