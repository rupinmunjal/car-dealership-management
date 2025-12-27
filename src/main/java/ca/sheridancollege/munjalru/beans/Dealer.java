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

    /** Operational status — defaults to {@link DealerStatus#ACTIVE}. */
    @Enumerated(EnumType.STRING)
    @Builder.Default
    private DealerStatus status = DealerStatus.ACTIVE;

    /** Public-facing display name (optional; falls back to {@link #name}). */
    private String displayName;

    /** Public-facing description (optional). */
    private String description;

    /** Whether the dealer appears in public listings. */
    @Builder.Default
    private boolean visible = true;

    @OneToMany(cascade = CascadeType.ALL)
    @JoinColumn(name = "dealer_id")
    @Builder.Default
    private List<Car> cars = new ArrayList<>();

    @OneToMany(mappedBy = "dealer")
    @Builder.Default
    private List<User> users = new ArrayList<>();

    /** The subscription package assigned to this dealer (nullable until assigned). */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "package_id")
    private Package dealerPackage;
}
