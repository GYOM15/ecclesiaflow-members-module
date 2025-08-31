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
 * Configuration OpenAPI pour documenter notre API
 */
@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("EcclesiaFlow Members Module API")
                .version("1.0")
                .description("API pour la gestion des membres d'EcclesiaFlow"));
    }

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
    
    private ApiResponse createApiResponse(String description, String schemaRef) {
        return new ApiResponse()
            .description(description)
            .content(new Content().addMediaType(
                org.springframework.http.MediaType.APPLICATION_JSON_VALUE,
                new MediaType().schema(new Schema<>().$ref(schemaRef))
            ));
    }
}
