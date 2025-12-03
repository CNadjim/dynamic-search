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
                description = "Numéro de la page (commence à 0)",
                example = "0",
                defaultValue = "0"
        )
        @Min(value = 0, message = "Le numéro de page doit être supérieur ou égal à 0")
        Integer page,

        @Schema(
                description = "Nombre d'éléments par page",
                example = "20",
                defaultValue = "20"
        )
        @Min(value = 1, message = "La taille de page doit être supérieure ou égale à 1")
        Integer size

) implements Serializable {

    /**
     * Constructeur compact avec valeurs par défaut
     */
    public PageRequest {
        // Valeurs par défaut si null
        if (page == null) {
            page = 0;
        }
        if (size == null) {
            size = 20;
        }
    }

    /**
     * Constructeur par défaut avec valeurs prédéfinies
     */
    public PageRequest() {
        this(0, 20);
    }

}
