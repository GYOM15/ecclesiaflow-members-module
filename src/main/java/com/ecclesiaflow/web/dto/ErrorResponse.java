package com.ecclesiaflow.web.dto;

import java.time.LocalDateTime;

/**
 * DTO représentant une réponse d'erreur standardisée pour l'API EcclesiaFlow.
 * <p>
 * Cette classe record encapsule les informations d'erreur retournées par l'API
 * lors d'exceptions ou d'erreurs de validation. Fournit un format cohérent
 * pour toutes les réponses d'erreur du module membres.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> DTO web - Réponse d'erreur standardisée</p>
 * 
 * <p><strong>Format de réponse :</strong></p>
 * <ul>
 *   <li>Horodatage précis de l'erreur</li>
 *   <li>Code de statut HTTP</li>
 *   <li>Type d'erreur (ex: "Bad Request", "Internal Server Error")</li>
 *   <li>Message descriptif de l'erreur</li>
 *   <li>Chemin de la requête qui a causé l'erreur</li>
 * </ul>
 * 
 * <p><strong>Utilisation :</strong></p>
 * <ul>
 *   <li>Gestion centralisée des erreurs via {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}</li>
 *   <li>Réponses d'erreur de validation</li>
 *   <li>Erreurs métier et techniques</li>
 *   <li>Documentation OpenAPI des réponses d'erreur</li>
 * </ul>
 * 
 * <p><strong>Avantages du record :</strong> Immutabilité, equals/hashCode automatiques,
 * sérialisation JSON native, construction concise.</p>
 * 
 * @param timestamp Horodatage de l'erreur
 * @param status Code de statut HTTP (400, 404, 500, etc.)
 * @param error Type d'erreur HTTP
 * @param message Message descriptif de l'erreur
 * @param path Chemin de la requête qui a causé l'erreur
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
public record ErrorResponse(LocalDateTime timestamp,
                            int status,
                            String error,
                            String message,
                            String path) {
}
