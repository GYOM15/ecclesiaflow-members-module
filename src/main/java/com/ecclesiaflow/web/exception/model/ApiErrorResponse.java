package com.ecclesiaflow.web.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Schema(description = "Réponse d'erreur standard de l'API")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
    @Schema(description = "Horodatage de l'erreur", example = "2023-01-01T12:00:00")
    LocalDateTime timestamp,

    @Schema(description = "Statut HTTP", example = "400")
    int status,

    @Schema(description = "Type d'erreur", example = "Bad Request")
    String error,

    @Schema(description = "Message d'erreur détaillé", example = "Erreur de validation des données")
    String message,

    @Schema(description = "Chemin de la requête", example = "/api/members")
    String path,

    @ArraySchema(schema = @Schema(implementation = ValidationError.class))
    List<ValidationError> errors
) {
    public ApiErrorResponse {
        if (errors == null) {
            errors = new ArrayList<>();
        }
    }

    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }

    public static class ApiErrorResponseBuilder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private int status;
        private String error;
        private String message;
        private String path;
        private List<ValidationError> errors = new ArrayList<>();

        public ApiErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        public ApiErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        public ApiErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        public ApiErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        public ApiErrorResponseBuilder addValidationError(ValidationError error) {
            this.errors.add(error);
            return this;
        }

        public ApiErrorResponse build() {
            return new ApiErrorResponse(timestamp, status, error, message, path, errors);
        }
    }
}
