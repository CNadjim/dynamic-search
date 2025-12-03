package io.github.cnadjim.dynamic.search.spring.starter.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.cnadjim.dynamic.search.spring.starter.response.FieldTypeResponse;
import io.github.cnadjim.dynamic.search.spring.starter.response.FilterOperatorResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;
import java.util.List;

/**
 * DTO REST - Critère de filtrage provenant du client
 * Record Java immuable pour les filtres de recherche
 * Utilise la convention camelCase pour les propriétés JSON
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Critère de filtrage pour la recherche dynamique")
public record FilterRequest(

        @Schema(
                description = "Nom du champ à filtrer",
                example = "name",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "La clé du filtre ne peut pas être vide")
        String key,

        @Schema(
                description = "Opérateur de filtrage",
                example = "equals",
                defaultValue = "equals",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "L'opérateur ne peut pas être null")
        FilterOperatorResponse operator,

        @Schema(
                description = "Type du champ (optionnel, déduit automatiquement depuis les métadonnées de l'entité si omis)",
                example = "string",
                defaultValue = "string",
                requiredMode = Schema.RequiredMode.NOT_REQUIRED
        )
        FieldTypeResponse fieldType,

        @Schema(
                description = "Valeur du filtre (utilisée pour la plupart des opérateurs)",
                example = "Windows"
        )
        Object value,

        @Schema(
                description = "Valeur de fin (utilisée pour l'opérateur BETWEEN)",
                example = "2024-12-31"
        )
        Object valueTo,

        @Schema(
                description = "Liste de valeurs (utilisée pour les opérateurs IN et NOT_IN)",
                example = "[\"Windows\", \"Linux\", \"MacOS\"]"
        )
        List<Object> values

) implements Serializable {

    /**
     * Constructeur compact avec valeurs par défaut
     */
    public FilterRequest {
        // Valeur par défaut pour l'opérateur si null
        if (operator == null) {
            operator = FilterOperatorResponse.EQUALS;
        }
        // fieldType peut être null - sera déduit depuis EntityMetadataStorage
    }

    /**
     * Constructeur de convenance pour les filtres simples
     */
    public FilterRequest(String key, FilterOperatorResponse operator, Object value) {
        this(key, operator, FieldTypeResponse.STRING, value, null, null);
    }

}
