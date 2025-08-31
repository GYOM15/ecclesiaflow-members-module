package com.ecclesiaflow.web.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.springdoc.core.customizers.OpenApiCustomizer;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configuration OpenAPI/Swagger pour la documentation automatique de l'API EcclesiaFlow Members.
 * <p>
 * Cette classe configure la génération automatique de la documentation OpenAPI 3.0
 * et personnalise l'affichage Swagger UI. Elle définit les informations générales
 * de l'API et ajoute automatiquement les réponses d'erreur standardisées à tous les endpoints.
 * </p>
 * 
 * <p><strong>Rôle architectural :</strong> Configuration - Documentation API automatique</p>
 * 
 * <p><strong>Responsabilités principales :</strong></p>
 * <ul>
 *   <li>Configuration des métadonnées de l'API (titre, version, description)</li>
 *   <li>Ajout automatique des réponses d'erreur standardisées (400, 401, 403, 404, 500)</li>
 *   <li>Personnalisation de l'interface Swagger UI</li>
 *   <li>Intégration avec le système de gestion d'erreurs {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler}</li>
 * </ul>
 * 
 * <p><strong>Fonctionnalités fournies :</strong></p>
 * <ul>
 *   <li>Documentation interactive via Swagger UI (/swagger-ui.html)</li>
 *   <li>Spécification OpenAPI JSON/YAML (/v3/api-docs)</li>
 *   <li>Réponses d'erreur cohérentes sur tous les endpoints</li>
 *   <li>Schémas d'erreur réutilisables (ApiErrorResponse, ValidationError)</li>
 * </ul>
 * 
 * <p><strong>Accès à la documentation :</strong></p>
 * <ul>
 *   <li>Interface Swagger UI : http://localhost:8080/swagger-ui.html</li>
 *   <li>Spécification JSON : http://localhost:8080/v3/api-docs</li>
 *   <li>Spécification YAML : http://localhost:8080/v3/api-docs.yaml</li>
 * </ul>
 * 
 * <p><strong>Garanties :</strong> Documentation toujours à jour, format standardisé, erreurs cohérentes.</p>
 * 
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see com.ecclesiaflow.web.exception.model.ApiErrorResponse
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
@Configuration
public class OpenApiConfig {

    /**
     * Configure les métadonnées principales de l'API OpenAPI.
     * <p>
     * Cette méthode définit les informations générales qui apparaissent dans
     * la documentation Swagger UI : titre, version, description de l'API.
     * </p>
     * 
     * @return une instance {@link OpenAPI} configurée avec les métadonnées du module
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EcclesiaFlow Members Module API")
                .version("1.0")
                .description("API pour la gestion des membres d'EcclesiaFlow"));
    }

    /**
     * Personnalise la spécification OpenAPI en ajoutant les réponses d'erreur globales.
     * <p>
     * Cette méthode ajoute automatiquement les réponses d'erreur standardisées
     * (400, 401, 403, 404, 500) à tous les endpoints de l'API. Cela garantit
     * une documentation cohérente et complète des cas d'erreur possibles.
     * </p>
     * 
     * @return un {@link OpenApiCustomizer} qui enrichit la spécification
     */
    @Bean
    public OpenApiCustomizer customerGlobalHeaderOpenApiCustomizer() {
        return openApi -> {
            openApi.getPaths().values().forEach(pathItem -> 
                pathItem.readOperations().forEach(operation -> {
                    ApiResponses apiResponses = operation.getResponses();
                    
                    // Réponse 400 - Bad Request
                    apiResponses.addApiResponse("400", createApiResponse(
                        "Requête invalide", 
                        "#/components/schemas/ApiErrorResponse"
                    ));
                    
                    // Réponse 401 - Unauthorized
                    apiResponses.addApiResponse("401", createApiResponse(
                        "Non autorisé", 
                        "#/components/schemas/ApiErrorResponse"
                    ));
                    
                    // Réponse 403 - Forbidden
                    apiResponses.addApiResponse("403", createApiResponse(
                        "Accès refusé", 
                        "#/components/schemas/ApiErrorResponse"
                    ));
                    
                    // Réponse 404 - Not Found
                    apiResponses.addApiResponse("404", createApiResponse(
                        "Ressource non trouvée", 
                        "#/components/schemas/ApiErrorResponse"
                    ));
                    
                    // Réponse 500 - Internal Server Error
                    apiResponses.addApiResponse("500", createApiResponse(
                        "Erreur interne du serveur", 
                        "#/components/schemas/ApiErrorResponse"
                    ));
                })
            );
        };
    }
    
    /**
     * Crée une réponse API standardisée avec référence au schéma d'erreur.
     * <p>
     * Cette méthode utilitaire génère des objets {@link ApiResponse} avec
     * une description et une référence vers le schéma d'erreur approprié.
     * </p>
     * 
     * @param description la description de la réponse d'erreur
     * @param schemaRef la référence vers le schéma OpenAPI (ex: "#/components/schemas/ApiErrorResponse")
     * @return une {@link ApiResponse} configurée avec contenu JSON
     */
    private ApiResponse createApiResponse(String description, String schemaRef) {
        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType().schema(new Schema<>().$ref(schemaRef))
            ));
    }
}
