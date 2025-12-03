package io.github.cnadjim.dynamic.search.spring.starter.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.cnadjim.dynamic.search.spring.starter.response.SortDirectionResponse;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.io.Serializable;

/**
 * DTO REST - Critère de tri provenant du client
 * Record Java immuable pour les tris de recherche
 * Utilise la convention camelCase pour les propriétés JSON
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Critère de tri pour la recherche dynamique")
public record SortRequest(

        @Schema(
                description = "Nom du champ sur lequel trier",
                example = "name",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotBlank(message = "La clé du tri ne peut pas être vide")
        String key,

        @Schema(
                description = "Direction du tri",
                example = "asc",
                defaultValue = "asc",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        @NotNull(message = "La direction ne peut pas être null")
        SortDirectionResponse direction

) implements Serializable {

    /**
     * Constructeur compact avec valeurs par défaut
     */
    public SortRequest {
        // Valeur par défaut pour la direction si null
        if (direction == null) {
            direction = SortDirectionResponse.ASC;
        }
    }

}
