package ca.sheridancollege.munjalru.models;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuthenticationResponse {
    private String token;
    private String role;

    /**
     * The dealer this user belongs to.
     * {@code null} for {@code SITE_ADMIN}.
     */
    private Long dealerId;

    /**
     * Granted permission names.
     * Empty for SITE_ADMIN and DEALER_ADMIN (they hold implicit permissions).
     */
    @Builder.Default
    private List<String> permissions = java.util.Collections.emptyList();
}
