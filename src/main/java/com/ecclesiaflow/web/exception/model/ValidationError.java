package com.ecclesiaflow.web.exception.model;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Détail d'une erreur de validation")
public record ValidationError(
    @Schema(description = "Message d'erreur", example = "Type invalide")
    String message,

    @Schema(description = "Chemin du champ en erreur", example = "user.age")
    String path,

    @Schema(description = "Type d'erreur", example = "type")
    String type,

    @Schema(description = "Type attendu", example = "string")
    String expected,

    @Schema(description = "Valeur reçue", example = "25")
    String received,

    @Schema(description = "Code d'erreur", example = "E_TYPE_MISMATCH")
    String code,

    @Schema(description = "Numéro de ligne", example = "6")
    Integer line,

    @Schema(description = "Numéro de colonne", example = "15")
    Integer column
) {}
