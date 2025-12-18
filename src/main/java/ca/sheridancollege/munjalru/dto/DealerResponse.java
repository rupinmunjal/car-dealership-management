package ca.sheridancollege.munjalru.dto;

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
    private String location;
    private List<CarResponse> cars;
}
