package ca.sheridancollege.munjalru.dto;

import ca.sheridancollege.munjalru.beans.DealerStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealerResponse {
    private Long id;
    private String name;
    private String adminEmail;
    private String location;
    private DealerStatus status;
    private String displayName;
    private String description;
    private boolean visible;
    private PackageResponse dealerPackage;
    private List<CarResponse> cars;
    private List<EmployeeResponse> employees;
}
