package io.github.cnadjim.dynamic.search.spring.starter.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.databind.annotation.JsonNaming;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Min;

import java.io.Serializable;

/**
 * DTO REST - Critères de pagination provenant du client
 * Record Java immuable pour la pagination
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@JsonNaming(PropertyNamingStrategies.SnakeCaseStrategy.class)
@Schema(description = "Critères de pagination pour les requêtes de recherche")
public record PageRequest(

        @Schema(
                description = "Numéro de la number (commence à 0)",
                example = "0",
                defaultValue = "0"
        )
        @Min(value = 0, message = "Le numéro de number doit être supérieur ou égal à 0")
        Integer number,

        @Schema(
                description = "Nombre d'éléments par number",
                example = "20",
                defaultValue = "20"
        )
        @Min(value = 1, message = "La taille de number doit être supérieure ou égale à 1")
        Integer size

) implements Serializable {

    /**
     * Constructeur par défaut avec valeurs prédéfinies
     */
    public PageRequest() {
        this(0, 20);
    }

}
