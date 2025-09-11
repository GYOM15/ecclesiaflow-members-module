package com.ecclesiaflow.web.exception.advices;

import com.ecclesiaflow.web.exception.*;
import com.ecclesiaflow.web.exception.model.ApiErrorResponse;
import com.ecclesiaflow.web.exception.model.ValidationError;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.validation.BindingResult;
import org.springframework.validation.FieldError;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.MissingRequestHeaderException;
import org.springframework.web.context.request.ServletWebRequest;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Tests unitaires pour GlobalExceptionHandler.
 * Vérifie la gestion centralisée des exceptions et la standardisation des réponses d'erreur.
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("GlobalExceptionHandler - Tests Unitaires")
class GlobalExceptionHandlerTest {

    @InjectMocks
    private GlobalExceptionHandler globalExceptionHandler;

    @Mock
    private MethodArgumentNotValidException methodArgumentNotValidException;

    @Mock
    private BindingResult bindingResult;

    private ServletWebRequest webRequest;
    private MockHttpServletRequest httpServletRequest;

    @BeforeEach
    void setUp() {
        httpServletRequest = new MockHttpServletRequest();
        httpServletRequest.setRequestURI("/ecclesiaflow/members");
        webRequest = new ServletWebRequest(httpServletRequest);
    }

    // === TESTS POUR LES ERREURS DE VALIDATION ===

    @Test
    @DisplayName("Devrait gérer les erreurs de validation Bean Validation (FieldError)")
    void handleValidationExceptions_ShouldReturnBadRequestWithValidationErrors() {
        // Given
        FieldError fieldError = new FieldError("memberRequest", "firstName", "Le prénom est obligatoire");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(fieldError));

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.error()).isEqualTo("Bad Request");
        assertThat(errorResponse.message()).isEqualTo("Erreur de validation des données");
        assertThat(errorResponse.path()).isEqualTo("/ecclesiaflow/members");
        assertThat(errorResponse.errors()).hasSize(1);

        ValidationError validationError = errorResponse.errors().get(0);
        assertThat(validationError.message()).isEqualTo("Le prénom est obligatoire");
        assertThat(validationError.path()).isEqualTo("firstName");
        assertThat(validationError.type()).isEqualTo("validation");
    }

    @Test
    @DisplayName("Devrait gérer les erreurs de validation Bean Validation (ObjectError)")
    void handleValidationExceptions_ShouldHandleObjectError() {
        // Given
        ObjectError objectError = new ObjectError("memberRequest", "Erreur générale de l'objet");
        when(methodArgumentNotValidException.getBindingResult()).thenReturn(bindingResult);
        when(bindingResult.getAllErrors()).thenReturn(List.of(objectError));

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleValidationExceptions(methodArgumentNotValidException, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.errors()).hasSize(1);

        ValidationError validationError = errorResponse.errors().get(0);
        assertThat(validationError.message()).isEqualTo("Erreur générale de l'objet");
        assertThat(validationError.path()).isEqualTo("memberRequest");
    }

    @Test
    @DisplayName("Devrait gérer les violations de contraintes (valeur non nulle et 'groups' non null)")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void handleConstraintViolation_ShouldReturnBadRequestWithConstraintErrors() {
        // Given
        ConstraintViolation violation = mock(ConstraintViolation.class);
        jakarta.validation.metadata.ConstraintDescriptor constraintDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        jakarta.validation.constraints.Size sizeAnnotation = mock(jakarta.validation.constraints.Size.class);
        jakarta.validation.Path propertyPath = mock(jakarta.validation.Path.class);

        when(violation.getMessage()).thenReturn("La taille doit être entre 2 et 50");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("firstName");
        when(violation.getInvalidValue()).thenReturn("A");
        when(violation.getConstraintDescriptor()).thenReturn(constraintDescriptor);
        when(constraintDescriptor.getAnnotation()).thenReturn(sizeAnnotation);
        when(sizeAnnotation.annotationType()).thenReturn((Class) jakarta.validation.constraints.Size.class);
        when(constraintDescriptor.getAttributes()).thenReturn(java.util.Map.of("groups", "someGroup"));

        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", (Set) Set.of(violation));

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleConstraintViolation(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).isEqualTo("Erreur de validation des contraintes");
        assertThat(errorResponse.errors()).hasSize(1);

        ValidationError validationError = errorResponse.errors().get(0);
        assertThat(validationError.message()).isEqualTo("La taille doit être entre 2 et 50");
        assertThat(validationError.path()).isEqualTo("firstName");
        assertThat(validationError.type()).isEqualTo("constraint");
        assertThat(validationError.path()).isEqualTo("firstName");
    }

    @Test
    @DisplayName("Devrait gérer les violations de contraintes (valeur nulle)")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void handleConstraintViolation_ShouldHandleNullInvalidValue() {
        // Given
        ConstraintViolation violation = mock(ConstraintViolation.class);
        jakarta.validation.metadata.ConstraintDescriptor constraintDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        jakarta.validation.constraints.NotNull notNullAnnotation = mock(jakarta.validation.constraints.NotNull.class);
        jakarta.validation.Path propertyPath = mock(jakarta.validation.Path.class);

        when(violation.getMessage()).thenReturn("La valeur ne peut pas être nulle");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("lastName");
        when(violation.getInvalidValue()).thenReturn(null); // Cas de valeur nulle
        when(violation.getConstraintDescriptor()).thenReturn(constraintDescriptor);
        when(constraintDescriptor.getAnnotation()).thenReturn(notNullAnnotation);
        when(notNullAnnotation.annotationType()).thenReturn((Class) jakarta.validation.constraints.NotNull.class);
        when(constraintDescriptor.getAttributes()).thenReturn(java.util.Map.of());

        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", (Set) Set.of(violation));

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleConstraintViolation(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).isEqualTo("Erreur de validation des contraintes");
        assertThat(errorResponse.errors()).hasSize(1);

        ValidationError validationError = errorResponse.errors().get(0);
        assertThat(validationError.message()).isEqualTo("La valeur ne peut pas être nulle");
        assertThat(validationError.path()).isEqualTo("lastName");
        assertThat(validationError.received()).isEqualTo("null");
    }

    @Test
    @DisplayName("Devrait gérer les violations de contraintes (attribut 'groups' null)")
    @SuppressWarnings({"unchecked", "rawtypes"})
    void handleConstraintViolation_ShouldHandleNullGroupsAttribute() {
        // Given
        ConstraintViolation violation = mock(ConstraintViolation.class);
        jakarta.validation.metadata.ConstraintDescriptor constraintDescriptor = mock(jakarta.validation.metadata.ConstraintDescriptor.class);
        jakarta.validation.constraints.Size sizeAnnotation = mock(jakarta.validation.constraints.Size.class);
        jakarta.validation.Path propertyPath = mock(jakarta.validation.Path.class);

        when(violation.getMessage()).thenReturn("La taille doit être entre 2 et 50");
        when(violation.getPropertyPath()).thenReturn(propertyPath);
        when(propertyPath.toString()).thenReturn("firstName");
        when(violation.getInvalidValue()).thenReturn("A");
        when(violation.getConstraintDescriptor()).thenReturn(constraintDescriptor);
        when(constraintDescriptor.getAnnotation()).thenReturn(sizeAnnotation);
        when(sizeAnnotation.annotationType()).thenReturn((Class) jakarta.validation.constraints.Size.class);
        java.util.Map<String, Object> attributesMap = new java.util.HashMap<>();
        attributesMap.put("groups", null); // Cas de groups null
        when(constraintDescriptor.getAttributes()).thenReturn(attributesMap);

        ConstraintViolationException exception = new ConstraintViolationException("Constraint violation", (Set) Set.of(violation));

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleConstraintViolation(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.errors()).hasSize(1);

        ValidationError validationError = errorResponse.errors().get(0);
        assertThat(validationError.code()).isEqualTo("CONSTRAINT_VIOLATION");
    }

    @Test
    @DisplayName("Devrait gérer les erreurs de JSON mal formé")
    void handleHttpMessageNotReadable_ShouldReturnBadRequest() {
        // Given
        HttpMessageNotReadableException exception = mock(HttpMessageNotReadableException.class);

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleHttpMessageNotReadable(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).isEqualTo("Requête JSON mal formée");
        assertThat(errorResponse.path()).isEqualTo("/ecclesiaflow/members");

        assertThat(errorResponse.errors().getFirst().type()).isEqualTo("parsing");
        assertThat(errorResponse.errors().getFirst().code()).isEqualTo("MalformedJson");

    }

    // === TESTS POUR LES EXCEPTIONS MÉTIER ===

    @Test
    @DisplayName("Devrait gérer MemberNotFoundException avec statut 404")
    void handleMemberNotFound_ShouldReturnNotFound() {
        // Given
        MemberNotFoundException exception = new MemberNotFoundException("Membre non trouvé avec l'ID: 123");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleMemberNotFound(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(404);
        assertThat(errorResponse.error()).isEqualTo("Not Found");
        assertThat(errorResponse.message()).isEqualTo("Membre non trouvé avec l'ID: 123");
    }

    @Test
    @DisplayName("Devrait gérer InvalidConfirmationCodeException avec statut 400")
    void handleInvalidConfirmationCode_ShouldReturnBadRequest() {
        // Given
        InvalidConfirmationCodeException exception = new InvalidConfirmationCodeException("Code de confirmation invalide");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleInvalidConfirmationCode(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).isEqualTo("Code de confirmation invalide");
    }

    @Test
    @DisplayName("Devrait gérer ExpiredConfirmationCodeException avec statut 400")
    void handleExpiredConfirmationCode_ShouldReturnBadRequest() {
        // Given
        ExpiredConfirmationCodeException exception = new ExpiredConfirmationCodeException("Code de confirmation expiré");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleExpiredConfirmationCode(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).isEqualTo("Code de confirmation expiré");
    }

    @Test
    @DisplayName("Devrait gérer MemberAlreadyConfirmedException avec statut 409")
    void handleMemberAlreadyConfirmed_ShouldReturnConflict() {
        // Given
        MemberAlreadyConfirmedException exception = new MemberAlreadyConfirmedException("Membre déjà confirmé");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleMemberAlreadyConfirmed(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(409);
        assertThat(errorResponse.error()).isEqualTo("Conflict");
        assertThat(errorResponse.message()).isEqualTo("Membre déjà confirmé");
    }

    @Test
    @DisplayName("Devrait gérer InvalidRequestException avec statut 400")
    void handleInvalidRequest_ShouldReturnBadRequest() {
        // Given
        InvalidRequestException exception = new InvalidRequestException("Requête invalide");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleInvalidRequest(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).isEqualTo("Requête invalide");
    }

    @Test
    @DisplayName("Devrait gérer IllegalArgumentException avec statut 400")
    void handleIllegalArgument_ShouldReturnBadRequest() {
        // Given
        IllegalArgumentException exception = new IllegalArgumentException("Argument invalide");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleIllegalArgument(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).isEqualTo("Argument invalide");
    }

    // === TESTS POUR LES ERREURS GÉNÉRIQUES ===

    @Test
    @DisplayName("Devrait gérer les exceptions génériques avec statut 500")
    void handleAllExceptions_ShouldReturnInternalServerError() {
        // Given
        RuntimeException exception = new RuntimeException("Erreur inattendue");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleAllExceptions(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.INTERNAL_SERVER_ERROR);
        assertThat(response.getBody()).isNotNull();

        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(500);
        assertThat(errorResponse.error()).isEqualTo("Internal Server Error");
        assertThat(errorResponse.message()).isEqualTo("Une erreur interne est survenue");
    }

    // === TESTS POUR LA STRUCTURE DES RÉPONSES ===

    @Test
    @DisplayName("Devrait inclure un timestamp dans toutes les réponses d'erreur")
    void allErrorResponses_ShouldIncludeTimestamp() {
        // Given
        MemberNotFoundException exception = new MemberNotFoundException("Test");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleMemberNotFound(exception, webRequest);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().timestamp()).isNotNull();
    }

    @Test
    @DisplayName("Devrait initialiser une liste d'erreurs vide pour les exceptions simples")
    void simpleExceptions_ShouldHaveEmptyErrorsList() {
        // Given
        MemberNotFoundException exception = new MemberNotFoundException("Test");

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleMemberNotFound(exception, webRequest);

        // Then
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().errors()).isNull();
    }

    // Exemple de test à ajouter
    @Test
    @DisplayName("Devrait gérer MissingRequestHeaderException avec statut 400")
    void handleMissingRequestHeader_ShouldReturnBadRequest() {
        // Given
        MissingRequestHeaderException exception = new MissingRequestHeaderException("Authorization", null); // Mockez selon votre besoin

        // When
        ResponseEntity<ApiErrorResponse> response = globalExceptionHandler
                .handleMissingRequestHeader(exception, webRequest);

        // Then
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(response.getBody()).isNotNull();
        ApiErrorResponse errorResponse = response.getBody();
        assertThat(errorResponse.status()).isEqualTo(400);
        assertThat(errorResponse.message()).contains("En-tête requis manquant: Authorization");
        assertThat(errorResponse.path()).isEqualTo("/ecclesiaflow/members");
        assertThat(errorResponse.errors()).hasSize(1); // Devrait avoir une erreur simple si buildBadRequestErrorResponse la crée
        assertThat(errorResponse.errors().get(0).message()).contains("En-tête requis manquant: Authorization");
    }
}