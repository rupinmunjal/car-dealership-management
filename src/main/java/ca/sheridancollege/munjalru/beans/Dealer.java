package ca.sheridancollege.munjalru.beans;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
@Entity
@Builder
@NoArgsConstructor
public class Dealer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String location;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "dealer_id")
    @Builder.Default
    private List<Car> cars = new ArrayList<>();

    @OneToMany(mappedBy = "dealer")
    @Builder.Default
    private List<User> users = new ArrayList<>();
}
