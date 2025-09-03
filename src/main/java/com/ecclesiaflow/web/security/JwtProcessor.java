package com.ecclesiaflow.web.security;

import com.ecclesiaflow.web.client.AuthClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/**
 * Service spécialisé dans la génération de tokens temporaires EcclesiaFlow.
 * <p>
 * Cette classe respecte le principe SRP en se concentrant uniquement sur
 * la génération de tokens temporaires via le module d'authentification.
 * Extrait cette responsabilité du service principal de confirmation pour
 * améliorer la séparation des préoccupations et la testabilité.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service délégateur - Génération de tokens</p>
 * 
 * <p><strong>Responsabilité unique :</strong></p>
 * <ul>
 *   <li>Génération de tokens temporaires pour la définition de mot de passe</li>
 *   <li>Délégation propre vers le module d'authentification</li>
 *   <li>Abstraction de la communication inter-modules</li>
 * </ul>
 * 
 * <p><strong>Avantages SRP :</strong></p>
 * <ul>
 *   <li>Responsabilité unique - Génération de tokens seulement</li>
 *   <li>Testabilité isolée de la logique de tokens</li>
 *   <li>Encapsulation de la dépendance au module d'authentification</li>
 *   <li>Évolution indépendante de la logique de tokens</li>
 * </ul>
 * 
 * <p><strong>Dépendances :</strong></p>
 * <ul>
 *   <li>{@link AuthClient} - Communication avec le module d'authentification</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, délégation propre, gestion d'erreurs
 * déléguée au module d'authentification, encapsulation des détails techniques.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see AuthClient
 */
@Service
@RequiredArgsConstructor
public class JwtProcessor {

    private final AuthClient authClient;

    /**
     * Génère un token temporaire pour permettre la définition du mot de passe.
     * <p>
     * Délègue la génération effective du token au module d'authentification
     * via {@link AuthClient}. Ce service agit comme une façade
     * spécialisée pour cette opération spécifique.
     * </p>
     * 
     * @param email l'email du membre confirmé pour lequel générer le token, non null
     * @return le token temporaire généré par le module d'authentification
     * 
     * @implNote Toute la logique de génération de token (JWT, expiration, signature)
     *           est déléguée au module d'authentification pour respecter SRP.
     */
    public String generateTemporaryToken(String email) {
        return authClient.generateTemporaryToken(email);
    }
}
