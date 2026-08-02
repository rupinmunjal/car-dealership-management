package ca.sheridancollege.munjalru.dto;

import com.fasterxml.jackson.databind.JsonNode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {
    private Long id;
    private Instant timestamp;
    private Long userId;
    private String actorEmail;
    private Long dealerId;
    private String action;
    private String entityType;
    private Long entityId;
    private JsonNode details;
}
