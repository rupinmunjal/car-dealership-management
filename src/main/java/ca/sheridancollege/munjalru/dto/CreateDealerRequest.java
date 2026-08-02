package ca.sheridancollege.munjalru.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateDealerRequest {

    @NotBlank(message = "Dealer name is required")
    private String name;

    @NotBlank(message = "Dealer location is required")
    private String location;

    @NotBlank(message = "Admin email is required")
    @Email(message = "Admin email must be valid")
    private String adminEmail;

    @NotBlank(message = "Admin password is required")
    private String adminPassword;

    private Long packageId;
    private String displayName;
    private String description;
}
