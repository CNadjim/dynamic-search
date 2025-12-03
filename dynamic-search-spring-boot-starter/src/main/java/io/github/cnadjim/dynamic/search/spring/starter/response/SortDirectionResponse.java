package io.github.cnadjim.dynamic.search.spring.starter.response;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.cnadjim.dynamic.search.model.SortDirection;

/**
 * DTO REST - Enum pour les directions de tri
 * Abstraction de l'enum du domaine pour la couche API REST
 * Sérialisé en lowercase dans les réponses JSON
 */
public enum SortDirectionResponse {
    ASC("asc"),
    DESC("desc");

    private final String value;

    SortDirectionResponse(String value) {
        this.value = value;
    }

    /**
     * Utilisé par Jackson pour la sérialisation JSON (en lowercase)
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Convertit le DTO vers l'enum du domaine
     */
    public SortDirection toDomain() {
        return SortDirection.valueOf(this.name());
    }

    /**
     * Crée un DTO depuis l'enum du domaine
     */
    public static SortDirectionResponse fromDomain(SortDirection direction) {
        return SortDirectionResponse.valueOf(direction.name());
    }
}
