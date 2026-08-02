package ca.sheridancollege.munjalru.repositories;

import ca.sheridancollege.munjalru.beans.Role;
import ca.sheridancollege.munjalru.beans.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    List<User> findByDealerIdAndRole(Long dealerId, Role role);

    Page<User> findByDealerIdAndRoleAndActiveTrue(Long dealerId, Role role, Pageable pageable);

    long countByDealerIdAndRoleAndActiveTrue(Long dealerId, Role role);

    Optional<User> findByIdAndDealerId(Long id, Long dealerId);
}
