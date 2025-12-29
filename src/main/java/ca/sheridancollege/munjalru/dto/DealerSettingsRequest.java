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
public class DealerSettingsRequest {

    private String displayName;
    private String description;

    private Boolean visible;
}
