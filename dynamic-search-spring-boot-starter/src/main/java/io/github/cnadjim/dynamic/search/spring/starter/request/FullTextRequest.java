package io.github.cnadjim.dynamic.search.spring.starter.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import java.io.Serializable;

/**
 * DTO REST - Requête de recherche full-text
 * Permet d'effectuer une recherche rapide sur tous les champs STRING searchable
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Requête de recherche full-text sur tous les champs STRING")
public record FullTextRequest(

        @Schema(
                description = "Texte à rechercher dans tous les champs STRING searchable",
                example = "Windows"
        )
        @NotBlank(message = "La requête full-text ne peut pas être vide")
        @Size(min = 1, max = 200, message = "La requête full-text doit contenir entre 1 et 200 caractères")
        String query

) implements Serializable {
}
