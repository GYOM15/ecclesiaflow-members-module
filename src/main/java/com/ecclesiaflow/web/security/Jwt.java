package com.ecclesiaflow.web.security;

import com.ecclesiaflow.business.domain.token.AuthenticationService;
import com.ecclesiaflow.web.client.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Implémentation JWT du générateur de tokens temporaires EcclesiaFlow.
 * <p>
 * Cette classe implémente l'interface {@link AuthenticationService} définie dans la couche business,
 * respectant ainsi le principe d'inversion de dépendance (DIP). Elle se concentre uniquement sur
 * la génération de tokens JWT temporaires via le module d'authentification externe.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Implémentation sécurité web - Génération de tokens JWT</p>
 * 
 * <p><strong>Responsabilité unique :</strong></p>
 * <ul>
 *   <li>Implémentation de {@link AuthenticationService} pour tokens JWT</li>
 *   <li>Délégation vers le module d'authentification externe</li>
 *   <li>Gestion de la sécurité des tokens temporaires</li>
 *   <li>Abstraction de la communication inter-modules</li>
 * </ul>
 * 
 * <p><strong>Avantages DIP :</strong></p>
 * <ul>
 *   <li>Implémentation dans la couche appropriée (web.security)</li>
 *   <li>Découplage du domaine métier via l'interface</li>
 *   <li>Testabilité améliorée avec mocks de l'interface</li>
 *   <li>Flexibilité pour changer d'implémentation</li>
 * </ul>
 * 
 * <p><strong>Dépendances :</strong></p>
 * <ul>
 *   <li>{@link AuthenticationService} - Interface du domaine métier (implémentée)</li>
 *   <li>{@link AuthClient} - Communication avec le module d'authentification</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, délégation propre, gestion d'erreurs
 * déléguée au module d'authentification, respect du contrat de l'interface.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see AuthenticationService
 * @see AuthClient
 */
@Service
@RequiredArgsConstructor
public class Jwt implements AuthenticationService {

    private final AuthClient authClient;

    @Override
    public String generateTemporaryToken(String email) {
        return authClient.generateTemporaryToken(email);
    }
}
