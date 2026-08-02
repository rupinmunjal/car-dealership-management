package ca.sheridancollege.munjalru.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DealerDashboardSummary implements Serializable {
    private Long dealerId;
    private String dealerName;
    private String location;
    private long carCount;
    private long employeeCount;
    private String packageName;
    private Integer maxEmployeeSeats;
    private Integer maxCarListings;
}
