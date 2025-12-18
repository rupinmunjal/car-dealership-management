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
public class DealerRequest {

    @NotBlank(message = "Dealer name is required")
    private String name;

    @NotBlank(message = "Dealer location is required")
    private String location;
}
