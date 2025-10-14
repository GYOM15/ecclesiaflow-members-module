package com.ecclesiaflow.business.domain.confirmation;

import org.springframework.stereotype.Service;

import java.util.UUID;

/**
 * Service spécialisé dans la génération de tokens de confirmation EcclesiaFlow.
 * <p>
 * Cette classe respecte le principe SRP en se concentrant uniquement sur
 * la génération de tokens de confirmation sécurisés. Extrait
 * la logique de génération de tokens du service principal pour améliorer
 * la séparation des responsabilités et la testabilité.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Service utilitaire - Génération de tokens</p>
 * 
 * <p><strong>Responsabilité unique :</strong></p>
 * <ul>
 *   <li>Génération de tokens de confirmation sécurisés (UUID)</li>
 *   <li>Garantie d'unicité et de sécurité cryptographique</li>
 *   <li>Algorithme de génération centralisé et réutilisable</li>
 * </ul>
 * 
 * <p><strong>Avantages SRP :</strong></p>
 * <ul>
 *   <li>Testabilité isolée de la logique de génération</li>
 *   <li>Réutilisabilité dans d'autres contextes</li>
 *   <li>Évolution indépendante de l'algorithme de génération</li>
 *   <li>Séparation claire des préoccupations</li>
 * </ul>
 * 
 * <p><strong>Format des tokens :</strong> UUID version 4 (ex: "550e8400-e29b-41d4-a716-446655440000")</p>
 * 
 * <p><strong>Sécurité :</strong></p>
 * <ul>
 *   <li>128 bits d'entropie (impossible à deviner)</li>
 *   <li>Génération cryptographiquement sécurisée</li>
 *   <li>Unicité garantie (probabilité de collision négligeable)</li>
 *   <li>Protection contre les attaques par force brute</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Thread-safe, tokens cryptographiquement sécurisés,
 * unicité garantie, performance optimale.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 */
@Service
public class ConfirmationTokenGenerator {

    /**
     * Génère un token de confirmation sécurisé (UUID v4).
     * <p>
     * Utilise {@link UUID#randomUUID()} pour générer un UUID version 4
     * cryptographiquement sécurisé avec 128 bits d'entropie.
     * Garantit l'unicité et l'impossibilité de deviner le token.
     * </p>
     * 
     * @return un token de confirmation UUID sécurisé
     * 
     * @implNote Utilise java.util.UUID qui génère des UUIDs v4 avec
     *           java.security.SecureRandom pour une entropie cryptographique.
     *           La probabilité de collision est négligeable (2^122 tokens possibles).
     */
    public UUID generateToken() {
        return UUID.randomUUID();
    }
}
