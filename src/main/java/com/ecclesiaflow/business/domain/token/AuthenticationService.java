package com.ecclesiaflow.business.domain.token;

/**
 * Interface pour la génération de tokens temporaires dans le contexte EcclesiaFlow.
 * <p>
 * Cette interface respecte le principe d'inversion de dépendance (DIP) en définissant
 * le contrat dans la couche business, permettant aux services métier de dépendre
 * d'une abstraction plutôt que d'une implémentation concrète.
 * </p>
 * 
 * <p><strong>Principe architectural :</strong> Interface de domaine - Génération de tokens</p>
 * 
 * <p><strong>Responsabilité :</strong></p>
 * <ul>
 *   <li>Définir le contrat pour la génération de tokens temporaires</li>
 *   <li>Abstraire la logique de génération de tokens du domaine métier</li>
 *   <li>Permettre l'inversion de dépendance entre couches</li>
 * </ul>
 * 
 * <p><strong>Avantages DIP :</strong></p>
 * <ul>
 *   <li>Découplage entre couche business et implémentation technique</li>
 *   <li>Testabilité améliorée avec mocks/stubs</li>
 *   <li>Flexibilité pour changer d'implémentation</li>
 *   <li>Respect des principes SOLID</li>
 * </ul>
 * 
 * <p><strong>Implémentations possibles :</strong></p>
 * <ul>
 *   <li>JWT via module d'authentification externe</li>
 *   <li>Tokens simples avec base de données</li>
 *   <li>Tokens cryptographiques locaux</li>
 * </ul>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
public interface AuthenticationService {

    /**
     * Génère un token temporaire pour permettre la définition du mot de passe.
     * <p>
     * Ce token est utilisé dans le processus de confirmation de compte
     * pour permettre à un membre confirmé de définir son mot de passe
     * de manière sécurisée.
     * </p>
     * 
     * @param email l'email du membre confirmé pour lequel générer le token, non null
     * @return le token temporaire généré, non null
     * 
     * @throws IllegalArgumentException si l'email est null ou vide
     * 
     * @implNote L'implémentation doit garantir :
     *           <ul>
     *           <li>Unicité du token généré</li>
     *           <li>Durée de vie limitée du token</li>
     *           <li>Sécurité cryptographique appropriée</li>
     *           <li>Thread-safety</li>
     *           <li>Gestion d'erreurs déléguée au service sous-jacent</li>
     *           </ul>
     */
    String generateTemporaryToken(String email);
}
