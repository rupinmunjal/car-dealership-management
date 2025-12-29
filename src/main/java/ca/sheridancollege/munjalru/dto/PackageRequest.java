package ca.sheridancollege.munjalru.dto;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PackageRequest {

    @NotBlank(message = "Package name is required")
    private String name;

    @Min(value = 1, message = "maxEmployeeSeats must be at least 1")
    private int maxEmployeeSeats;

    @Min(value = 1, message = "maxCarListings must be at least 1")
    private int maxCarListings;
}
