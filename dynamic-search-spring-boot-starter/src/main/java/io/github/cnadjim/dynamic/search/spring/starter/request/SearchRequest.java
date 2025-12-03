package io.github.cnadjim.dynamic.search.spring.starter.request;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * DTO REST - Requête de recherche provenant du client
 * Détail d'implémentation de l'API REST (contrat externe)
 * Record Java immuable pour les requêtes de recherche
 * Utilise la convention camelCase pour les propriétés JSON
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Requête de recherche dynamique avec filtres, tris et pagination")
public record SearchRequest(

        @Schema(
                description = "Liste des critères de filtrage à appliquer",
                example = "[{\"key\": \"name\", \"operator\": \"contains\", \"value\": \"Windows\"}]"
        )
        @Valid
        @Size(max = 50, message = "Le nombre maximum de filtres est de 50")
        List<FilterRequest> filters,

        @Schema(
                description = "Liste des critères de tri à appliquer",
                example = "[{\"key\": \"name\", \"direction\": \"asc\"}]"
        )
        @Valid
        @Size(max = 10, message = "Le nombre maximum de tris est de 10")
        List<SortRequest> sorts,

        @Schema(
                description = "Critères de pagination (page et size)",
                implementation = PageRequest.class
        )
        @Valid
        PageRequest page

) implements Serializable {

    /**
     * Constructeur compact avec valeurs par défaut
     */
    public SearchRequest {
        // Initialiser les listes vides si null
        if (filters == null) {
            filters = new ArrayList<>();
        }

        if (sorts == null) {
            sorts = new ArrayList<>();
        }

        // Initialiser la pagination par défaut si null
        if (page == null) {
            page = new PageRequest();
        }
    }

    /**
     * Constructeur par défaut avec valeurs prédéfinies
     */
    public SearchRequest() {
        this(new ArrayList<>(), new ArrayList<>(), new PageRequest());
    }

    /**
     * Getter pour les filtres (garantit une liste non-null)
     */
    public List<FilterRequest> getFilters() {
        return filters != null ? filters : new ArrayList<>();
    }

    /**
     * Getter pour les tris (garantit une liste non-null)
     */
    public List<SortRequest> getSorts() {
        return sorts != null ? sorts : new ArrayList<>();
    }

    /**
     * Getter pour la pagination (garantit une valeur non-null)
     */
    public PageRequest getPageRequest() {
        return page != null ? page : new PageRequest();
    }

}
