package com.ecclesiaflow.web.security;

import com.ecclesiaflow.business.domain.member.Member;
import com.ecclesiaflow.business.domain.member.MemberRepository;
import com.ecclesiaflow.business.domain.member.MemberStatus;
import com.ecclesiaflow.web.exception.model.ApiErrorResponse;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Optional;
import java.util.Set;

/**
 * Application-level filter that blocks requests from deactivated, suspended, or inactive members.
 * <p>
 * Placed after {@code BearerTokenAuthenticationFilter} in the Spring Security filter chain.
 * Instead of disabling users in Keycloak (which locks them out entirely), this filter allows
 * authenticated users to reach the application, then checks their member status and returns
 * a machine-readable 403 response if they are not in an allowed state.
 * </p>
 * <p>
 * This pattern (application-level blocking, not IdP-level) enables the re-login reactivation
 * flow: deactivated users can still authenticate via Keycloak but are blocked from accessing
 * resources, except the reactivation endpoint.
 * </p>
 * <p>
 * Not annotated with {@code @Component} — registered as a bean via {@link SecurityConfig}
 * to avoid being picked up by {@code @WebMvcTest} sliced contexts.
 * </p>
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@RequiredArgsConstructor
public class MemberStatusFilter extends OncePerRequestFilter {

    private final MemberRepository memberRepository;
    private final ObjectMapper objectMapper;

    private static final Set<String> EXCLUDED_PATHS = Set.of(
        "/ecclesiaflow/members",
        "/ecclesiaflow/members/confirmation",
        "/ecclesiaflow/members/new-confirmation",
        "/ecclesiaflow/members/me/email/confirm",
        "/ecclesiaflow/members/me/reactivate"
    );

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        return EXCLUDED_PATHS.contains(path)
            || path.startsWith("/actuator/");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {

        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (!(authentication instanceof JwtAuthenticationToken jwtAuth)) {
            filterChain.doFilter(request, response);
            return;
        }

        String keycloakUserId = jwtAuth.getToken().getSubject();
        Optional<Member> memberOpt = memberRepository.getByKeycloakUserId(keycloakUserId);

        if (memberOpt.isEmpty()) {
            // No member record yet (e.g. SSO onboarding in progress)
            filterChain.doFilter(request, response);
            return;
        }

        Member member = memberOpt.get();
        MemberStatus status = member.getStatus();

        switch (status) {
            case DEACTIVATED -> writeErrorResponse(response, request,
                "ACCOUNT_DEACTIVATED",
                "Your account has been deactivated. You can reactivate it within the grace period.");
            case SUSPENDED -> writeErrorResponse(response, request,
                "ACCOUNT_SUSPENDED",
                "Your account has been suspended. Please contact your administrator.");
            case INACTIVE -> writeErrorResponse(response, request,
                "ACCOUNT_INACTIVE",
                "Your account is no longer active.");
            default -> filterChain.doFilter(request, response);
        }
    }

    private void writeErrorResponse(HttpServletResponse response,
                                    HttpServletRequest request,
                                    String errorCode,
                                    String message) throws IOException {
        ApiErrorResponse errorResponse = ApiErrorResponse.builder()
            .status(403)
            .error("Forbidden")
            .errorCode(errorCode)
            .message(message)
            .path(request.getRequestURI())
            .build();

        response.setStatus(403);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        objectMapper.writeValue(response.getOutputStream(), errorResponse);
    }
}
