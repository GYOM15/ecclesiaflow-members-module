package com.ecclesiaflow.web.delegate;

import com.ecclesiaflow.business.security.RequireScopes;
import com.ecclesiaflow.business.services.MemberService;
import com.ecclesiaflow.web.model.MembersGetConfirmationStatus200Response;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

/**
 * Délégué pour les endpoints temporaires des membres - Pattern Delegate avec OpenAPI Generator.
 * <p>
 * Ce délégué contient la logique métier pour les endpoints temporaires qui seront
 * éventuellement migrés ou supprimés dans les futures versions.
 * </p>
 * 
 * <p><strong>Endpoints gérés :</strong></p>
 * <ul>
 *   <li>GET /ecclesiaflow/hello - Test d'authentification</li>
 *   <li>GET /ecclesiaflow/members/{email}/confirmation-status - Vérification du statut de confirmation</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 2.0.0
 */
@Service
@RequiredArgsConstructor
public class MembersTemporaryDelegate {

    private final MemberService memberService;

    /**
     * Endpoint de test d'authentification pour les membres.
     * 
     * @return Message de bienvenue "Hi Member"
     */
    @RequireScopes("ef:members:read:own")
    public ResponseEntity<String> sayHello() {
        return ResponseEntity.ok("Hi Member");
    }

    /**
     * Récupère le statut de confirmation d'un membre par email.
     * 
     * @param email Adresse email du membre
     * @return Objet contenant le statut de confirmation
     */
    public ResponseEntity<MembersGetConfirmationStatus200Response> getMemberConfirmationStatus(String email) {

        boolean isConfirmed = memberService.isEmailConfirmed(email);
        
        MembersGetConfirmationStatus200Response response = new MembersGetConfirmationStatus200Response();
        response.setConfirmed(isConfirmed);
        
        return ResponseEntity.ok(response);
    }
}
