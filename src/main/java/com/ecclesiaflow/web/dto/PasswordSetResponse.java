package com.ecclesiaflow.web.dto;

import lombok.Builder;
import lombok.Data;

/**
 * DTO de réponse pour les opérations de définition de mot de passe EcclesiaFlow.
 * <p>
 * Cette classe représente la réponse HTTP standardisée retournée après
 * une tentative de définition de mot de passe initial. Fournit des informations
 * claires sur le succès de l'opération avec un message utilisateur et un
 * statut programmatique pour le traitement côté client.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> DTO de réponse - Couche web vers client</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Encapsulation de la réponse de succès pour la définition de mot de passe</li>
 *   <li>Fourniture d'un message utilisateur lisible et informatif</li>
 *   <li>Fourniture d'un statut programmatique pour le traitement automatisé</li>
 *   <li>Standardisation des réponses API pour la cohérence client</li>
 * </ul>
 * 
 * <p><strong>Format de réponse JSON :</strong></p>
 * <pre>
 * {
 *   "message": "Mot de passe défini avec succès",
 *   "status": "SUCCESS"
 * }
 * </pre>
 * 
 * <p><strong>Utilisation côté client :</strong></p>
 * <ul>
 *   <li>Affichage du message à l'utilisateur pour confirmation visuelle</li>
 *   <li>Vérification du statut pour la logique conditionnelle</li>
 *   <li>Navigation automatique vers l'écran de connexion après succès</li>
 * </ul>
 * 
 * <p><strong>Patterns utilisés :</strong></p>
 * <ul>
 *   <li>Builder Pattern - Construction d'objet fluide</li>
 *   <li>Data Transfer Object - Transport de données vers le client</li>
 *   <li>Response Pattern - Standardisation des réponses API</li>
 * </ul>
 * 
 * <p><strong>Extensibilité :</strong></p>
 * <ul>
 *   <li>Possibilité d'ajouter des champs supplémentaires (timestamp, etc.)</li>
 *   <li>Support de l'internationalisation via le champ message</li>
 *   <li>Statuts multiples pour différents scénarios de réponse</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Sérialisation JSON automatique, construction via Builder,
 * cohérence des réponses API, lisibilité côté client.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see PasswordSetMapper#toSuccessResponse()
 * @see MemberPasswordController
 */
@Data
@Builder
public class PasswordSetResponse {
    
    /**
     * Message descriptif destiné à l'affichage utilisateur.
     * <p>
     * Message en langage naturel décrivant le résultat de l'opération
     * de définition de mot de passe. Conçu pour être affiché directement
     * dans l'interface utilisateur comme confirmation de succès.
     * </p>
     * 
     * @implNote Actuellement en français. Pour l'internationalisation future,
     *           ce champ pourrait être généré dynamiquement selon la locale.
     */
    private String message;
    
    /**
     * Statut programmatique de l'opération pour le traitement automatisé.
     * <p>
     * Code de statut standardisé permettant au client de déterminer
     * programmatiquement le résultat de l'opération sans parser le message.
     * Facilite la logique conditionnelle côté client.
     * </p>
     * 
     * @implNote Valeurs possibles : "SUCCESS", "ERROR", "PENDING".
     *           Actuellement seul "SUCCESS" est utilisé pour cette réponse.
     */
    private String status;
}
