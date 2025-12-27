package ca.sheridancollege.munjalru.beans;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * A subscription tier that governs a dealer's limits.
 *
 * <p>When a dealer is downgraded to a package with fewer seats than their
 * current active employee count, existing employees are <em>not</em>
 * retroactively deactivated — only new hires are blocked until the count
 * drops below the new limit.</p>
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "dealer_package")
public class Package {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String name;

    @Column(nullable = false)
    private int maxEmployeeSeats;

    @Column(nullable = false)
    private int maxCarListings;
}
