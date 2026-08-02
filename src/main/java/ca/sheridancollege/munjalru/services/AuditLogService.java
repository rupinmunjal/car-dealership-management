package ca.sheridancollege.munjalru.services;

import ca.sheridancollege.munjalru.beans.AuditLog;
import ca.sheridancollege.munjalru.beans.User;
import ca.sheridancollege.munjalru.dto.AuditLogResponse;
import ca.sheridancollege.munjalru.repositories.AuditLogRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuditLogService {

    private final AuditLogRepository auditLogRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void record(String action, String entityType, Long entityId, Long dealerId, Object details) {
        User actor = currentActor();
        auditLogRepository.save(AuditLog.builder()
                .userId(actor == null ? null : actor.getId())
                .actorEmail(actor == null ? null : actor.getEmail())
                .dealerId(dealerId)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .details(writeDetails(details))
                .build());
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findAll(Pageable pageable) {
        return auditLogRepository.findAll(pageable).map(this::toResponse);
    }

    @Transactional(readOnly = true)
    public Page<AuditLogResponse> findByDealer(Long dealerId, Pageable pageable) {
        return auditLogRepository.findByDealerId(dealerId, pageable).map(this::toResponse);
    }

    private User currentActor() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null && authentication.getPrincipal() instanceof User user ? user : null;
    }

    private String writeDetails(Object details) {
        if (details == null) {
            return null;
        }
        try {
            return objectMapper.writeValueAsString(details);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Could not serialize audit details", exception);
        }
    }

    private AuditLogResponse toResponse(AuditLog log) {
        JsonNode details = null;
        if (log.getDetails() != null) {
            try {
                details = objectMapper.readTree(log.getDetails());
            } catch (JsonProcessingException exception) {
                details = objectMapper.getNodeFactory().textNode(log.getDetails());
            }
        }
        return AuditLogResponse.builder()
                .id(log.getId())
                .timestamp(log.getTimestamp())
                .userId(log.getUserId())
                .actorEmail(log.getActorEmail())
                .dealerId(log.getDealerId())
                .action(log.getAction())
                .entityType(log.getEntityType())
                .entityId(log.getEntityId())
                .details(details)
                .build();
    }
}
