package com.ecclesiaflow.web.config;

import com.ecclesiaflow.application.config.OpenApiConfig;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.Operation;
import io.swagger.v3.oas.models.PathItem;
import io.swagger.v3.oas.models.Paths;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springdoc.core.customizers.OpenApiCustomizer;

import static org.assertj.core.api.Assertions.*;

/**
 * Tests unitaires pour OpenApiConfig.
 * Vérifie la configuration OpenAPI et l'ajout automatique des réponses d'erreur.
 */
@DisplayName("OpenApiConfig - Unit Tests")
class OpenApiConfigTest {

    private OpenApiConfig openApiConfig;

    @BeforeEach
    void setUp() {
        openApiConfig = new OpenApiConfig();
    }

    // === OPENAPI CONFIGURATION TESTS ===

    @Test
    @DisplayName("Should create an OpenAPI configuration with correct metadata")
    void customOpenAPI_ShouldCreateConfigurationWithCorrectMetadata() {
        // When
        OpenAPI openAPI = openApiConfig.customOpenAPI();

        // Then
        assertThat(openAPI).isNotNull();

        Info info = openAPI.getInfo();
        assertThat(info).isNotNull();
        assertThat(info.getTitle()).isEqualTo("EcclesiaFlow Members Module API");
        assertThat(info.getVersion()).isEqualTo("1.0");
        assertThat(info.getDescription()).isEqualTo("API pour la gestion des membres d'EcclesiaFlow");
    }

    // === GLOBAL CUSTOMIZER TESTS ===

    @Test
    @DisplayName("Should create a customizer that adds global error responses")
    void customerGlobalHeaderOpenApiCustomizer_ShouldCreateCustomizer() {
        // When
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();

        // Then
        assertThat(customizer).isNotNull();
    }

    @Test
    @DisplayName("Should add all standard error responses to an operation")
    void customizer_ShouldAddAllStandardErrorResponses() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPI();

        // When
        customizer.customise(openAPI);

        // Then
        Operation operation = getFirstOperation(openAPI);
        ApiResponses responses = operation.getResponses();

        // Verify that all error responses are added
        assertThat(responses.get("400")).isNotNull();
        assertThat(responses.get("401")).isNotNull();
        assertThat(responses.get("403")).isNotNull();
        assertThat(responses.get("404")).isNotNull();
        assertThat(responses.get("500")).isNotNull();
    }

    @Test
    @DisplayName("Should correctly configure the 400 Bad Request response")
    void customizer_ShouldConfigureBadRequestResponse() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPI();

        // When
        customizer.customise(openAPI);

        // Then
        Operation operation = getFirstOperation(openAPI);
        ApiResponse badRequestResponse = operation.getResponses().get("400");

        assertThat(badRequestResponse).isNotNull();
        assertThat(badRequestResponse.getDescription()).isEqualTo("Requête invalide");

        Content content = badRequestResponse.getContent();
        assertThat(content).isNotNull();

        MediaType mediaType = content.get("application/json");
        assertThat(mediaType).isNotNull();
        assertThat(mediaType.getSchema()).isNotNull();
        assertThat(mediaType.getSchema().get$ref()).isEqualTo("#/components/schemas/ApiErrorResponse");
    }

    @Test
    @DisplayName("Should correctly configure the 401 Unauthorized response")
    void customizer_ShouldConfigureUnauthorizedResponse() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPI();

        // When
        customizer.customise(openAPI);

        // Then
        Operation operation = getFirstOperation(openAPI);
        ApiResponse unauthorizedResponse = operation.getResponses().get("401");

        assertThat(unauthorizedResponse).isNotNull();
        assertThat(unauthorizedResponse.getDescription()).isEqualTo("Non autorisé");
        assertThat(unauthorizedResponse.getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ApiErrorResponse");
    }

    @Test
    @DisplayName("Should correctly configure the 403 Forbidden response")
    void customizer_ShouldConfigureForbiddenResponse() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPI();

        // When
        customizer.customise(openAPI);

        // Then
        Operation operation = getFirstOperation(openAPI);
        ApiResponse forbiddenResponse = operation.getResponses().get("403");

        assertThat(forbiddenResponse).isNotNull();
        assertThat(forbiddenResponse.getDescription()).isEqualTo("Accès refusé");
        assertThat(forbiddenResponse.getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ApiErrorResponse");
    }

    @Test
    @DisplayName("Should correctly configure the 404 Not Found response")
    void customizer_ShouldConfigureNotFoundResponse() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPI();

        // When
        customizer.customise(openAPI);

        // Then
        Operation operation = getFirstOperation(openAPI);
        ApiResponse notFoundResponse = operation.getResponses().get("404");

        assertThat(notFoundResponse).isNotNull();
        assertThat(notFoundResponse.getDescription()).isEqualTo("Ressource non trouvée");
        assertThat(notFoundResponse.getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ApiErrorResponse");
    }

    @Test
    @DisplayName("Should correctly configure the 500 Internal Server Error response")
    void customizer_ShouldConfigureInternalServerErrorResponse() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPI();

        // When
        customizer.customise(openAPI);

        // Then
        Operation operation = getFirstOperation(openAPI);
        ApiResponse serverErrorResponse = operation.getResponses().get("500");

        assertThat(serverErrorResponse).isNotNull();
        assertThat(serverErrorResponse.getDescription()).isEqualTo("Erreur interne du serveur");
        assertThat(serverErrorResponse.getContent().get("application/json").getSchema().get$ref())
                .isEqualTo("#/components/schemas/ApiErrorResponse");
    }

    @Test
    @DisplayName("Should process multiple operations in multiple paths")
    void customizer_ShouldProcessMultipleOperationsInMultiplePaths() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPIWithMultipleOperations();

        // When
        customizer.customise(openAPI);

        // Then
        Paths paths = openAPI.getPaths();

        // Verify the first path
        PathItem firstPath = paths.get("/api/members");
        Operation getOperation = firstPath.getGet();
        Operation postOperation = firstPath.getPost();

        assertThat(getOperation.getResponses().get("400")).isNotNull();
        assertThat(getOperation.getResponses().get("404")).isNotNull();
        assertThat(postOperation.getResponses().get("400")).isNotNull();
        assertThat(postOperation.getResponses().get("500")).isNotNull();

        // Verify the second path
        PathItem secondPath = paths.get("/api/members/{id}");
        Operation putOperation = secondPath.getPut();

        assertThat(putOperation.getResponses().get("400")).isNotNull();
        assertThat(putOperation.getResponses().get("404")).isNotNull();
    }

    @Test
    @DisplayName("Should preserve existing responses when adding error responses")
    void customizer_ShouldPreserveExistingResponses() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = createTestOpenAPI();

        // Add an existing response
        Operation operation = getFirstOperation(openAPI);
        operation.getResponses().addApiResponse("200", new ApiResponse().description("Success"));

        // When
        customizer.customise(openAPI);

        // Then
        ApiResponses responses = operation.getResponses();

        // The existing response should be preserved
        assertThat(responses.get("200")).isNotNull();
        assertThat(responses.get("200").getDescription()).isEqualTo("Success");

        // New error responses should be added
        assertThat(responses.get("400")).isNotNull();
        assertThat(responses.get("404")).isNotNull();
        assertThat(responses.get("500")).isNotNull();
    }

    @Test
    @DisplayName("Should handle an OpenAPI without paths")
    void customizer_ShouldHandleOpenAPIWithoutPaths() {
        // Given
        OpenApiCustomizer customizer = openApiConfig.customerGlobalHeaderOpenApiCustomizer();
        OpenAPI openAPI = new OpenAPI();
        openAPI.setPaths(new Paths());

        // When & Then
        assertThatCode(() -> customizer.customise(openAPI))
                .doesNotThrowAnyException();
    }

    // === UTILITY METHODS FOR TESTS ===

    private OpenAPI createTestOpenAPI() {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        PathItem pathItem = new PathItem();
        Operation operation = new Operation();
        operation.setResponses(new ApiResponses());
        pathItem.setGet(operation);

        paths.addPathItem("/api/members", pathItem);
        openAPI.setPaths(paths);

        return openAPI;
    }

    private OpenAPI createTestOpenAPIWithMultipleOperations() {
        OpenAPI openAPI = new OpenAPI();
        Paths paths = new Paths();

        // First path with GET and POST
        PathItem firstPath = new PathItem();

        Operation getOperation = new Operation();
        getOperation.setResponses(new ApiResponses());
        firstPath.setGet(getOperation);

        Operation postOperation = new Operation();
        postOperation.setResponses(new ApiResponses());
        firstPath.setPost(postOperation);

        paths.addPathItem("/api/members", firstPath);

        // Second path with PUT
        PathItem secondPath = new PathItem();

        Operation putOperation = new Operation();
        putOperation.setResponses(new ApiResponses());
        secondPath.setPut(putOperation);

        paths.addPathItem("/api/members/{id}", secondPath);

        openAPI.setPaths(paths);
        return openAPI;
    }

    private Operation getFirstOperation(OpenAPI openAPI) {
        return openAPI.getPaths().values().iterator().next().readOperations().get(0);
    }
}