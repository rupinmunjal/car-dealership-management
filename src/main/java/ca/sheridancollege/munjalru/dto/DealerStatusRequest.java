package ca.sheridancollege.munjalru.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealerStatusRequest {
    @NotBlank(message = "Status is required")
    private String status;  // "ACTIVE" or "SUSPENDED"
}
