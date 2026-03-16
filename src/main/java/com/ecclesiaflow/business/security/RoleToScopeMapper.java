package com.ecclesiaflow.business.security;

import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * Maps Keycloak roles to application-level scopes.
 *
 * <p>Phase 1 of the authorization strategy: scopes are derived from Keycloak
 * roles at the application layer. Phase 2 (production) will migrate to
 * Keycloak Authorization Services for multi-tenant isolation by church_id.</p>
 *
 * <p>Role hierarchy: USER has own-resource scopes, ADMIN and SUPER_ADMIN
 * have full access (own + all).</p>
 */
@Component
public class RoleToScopeMapper {

    private static final Map<String, Set<String>> ROLE_SCOPE_MAPPING = Map.of(
            "USER", Set.of(
                    "ef:members:read:own",
                    "ef:members:write:own",
                    "ef:members:delete:own"
            ),
            "ADMIN", Set.of(
                    "ef:members:read:own", "ef:members:read:all",
                    "ef:members:write:own", "ef:members:write:all",
                    "ef:members:delete:own", "ef:members:delete:all"
            ),
            "SUPER_ADMIN", Set.of(
                    "ef:members:read:own", "ef:members:read:all",
                    "ef:members:write:own", "ef:members:write:all",
                    "ef:members:delete:own", "ef:members:delete:all"
            )
    );

    /**
     * Derives scopes from the user's Keycloak roles (union of all role mappings).
     *
     * @param roles set of role names (without ROLE_ prefix)
     * @return union of scopes granted by all matching roles
     */
    public Set<String> getScopesForRoles(Set<String> roles) {
        return roles.stream()
                .map(role -> ROLE_SCOPE_MAPPING.getOrDefault(role, Set.of()))
                .flatMap(Set::stream)
                .collect(Collectors.toSet());
    }
}
