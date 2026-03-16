package com.ecclesiaflow.web.exception.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Standardized error response record with validation error support.
 * <p>
 * Handles detailed validation errors. Primarily used by
 * {@link com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler} for
 * Bean Validation errors and complex business errors.
 * </p>
 *
 * @param timestamp  error timestamp
 * @param status     HTTP status code
 * @param error      HTTP error type
 * @param errorCode  machine-readable error code (e.g. ACCOUNT_DEACTIVATED), nullable
 * @param message    detailed error message
 * @param path       request path
 * @param errors     detailed validation errors
 *
 * @author EcclesiaFlow Team
 * @since 1.0.0
 * @see ValidationError
 * @see com.ecclesiaflow.web.exception.advices.GlobalExceptionHandler
 */
@Schema(description = "Standard API error response")
@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiErrorResponse(
    @Schema(description = "Error timestamp", example = "2023-01-01T12:00:00")
    LocalDateTime timestamp,

    @Schema(description = "HTTP status code", example = "400")
    int status,

    @Schema(description = "HTTP error type", example = "Bad Request")
    String error,

    @Schema(description = "Machine-readable error code", example = "ACCOUNT_DEACTIVATED")
    String errorCode,

    @Schema(description = "Detailed error message", example = "Validation error")
    String message,

    @Schema(description = "Request path", example = "/api/members")
    String path,

    @ArraySchema(schema = @Schema(implementation = ValidationError.class))
    List<ValidationError> errors
) {
    public ApiErrorResponse {
        // Keep errors null if explicitly passed as null
    }

    /**
     * Creates a new builder instance.
     *
     * @return new builder
     */
    public static ApiErrorResponseBuilder builder() {
        return new ApiErrorResponseBuilder();
    }

    /**
     * Fluent builder for constructing an ApiErrorResponse.
     */
    public static class ApiErrorResponseBuilder {
        private LocalDateTime timestamp = LocalDateTime.now();
        private int status;
        private String error;
        private String errorCode;
        private String message;
        private String path;
        private List<ValidationError> errors = null;

        /**
         * Sets the HTTP status code.
         *
         * @param status HTTP status code (400, 404, 500, etc.)
         * @return this builder for chaining
         */
        public ApiErrorResponseBuilder status(int status) {
            this.status = status;
            return this;
        }

        /**
         * Sets the HTTP error type.
         *
         * @param error error type ("Bad Request", "Not Found", etc.)
         * @return this builder for chaining
         */
        public ApiErrorResponseBuilder error(String error) {
            this.error = error;
            return this;
        }

        /**
         * Sets the machine-readable error code.
         *
         * @param errorCode error code (e.g. "ACCOUNT_DEACTIVATED")
         * @return this builder for chaining
         */
        public ApiErrorResponseBuilder errorCode(String errorCode) {
            this.errorCode = errorCode;
            return this;
        }

        /**
         * Sets the error message.
         *
         * @param message descriptive error message
         * @return this builder for chaining
         */
        public ApiErrorResponseBuilder message(String message) {
            this.message = message;
            return this;
        }

        /**
         * Sets the request path that caused the error.
         *
         * @param path HTTP request path
         * @return this builder for chaining
         */
        public ApiErrorResponseBuilder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Adds a validation error to the list.
         *
         * @param error validation error to add
         * @return this builder for chaining
         */
        public ApiErrorResponseBuilder addValidationError(ValidationError error) {
            if (this.errors == null) {
                this.errors = new ArrayList<>();
            }
            this.errors.add(error);
            return this;
        }

        /**
         * Sets the validation errors list explicitly.
         *
         * @param errors list of errors (may be null)
         * @return this builder for chaining
         */
        public ApiErrorResponseBuilder errors(List<ValidationError> errors) {
            this.errors = errors;
            return this;
        }

        /**
         * Builds the final ApiErrorResponse.
         *
         * @return new ApiErrorResponse instance
         */
        public ApiErrorResponse build() {
            return new ApiErrorResponse(timestamp, status, error, errorCode, message, path, errors);
        }
    }
}
