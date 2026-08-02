package ca.sheridancollege.munjalru.repositories;

import ca.sheridancollege.munjalru.beans.AuditLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AuditLogRepository extends JpaRepository<AuditLog, Long> {
    Page<AuditLog> findByDealerId(Long dealerId, Pageable pageable);
}
