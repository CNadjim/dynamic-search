package io.github.cnadjim.dynamic.search.spring.starter.response;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.cnadjim.dynamic.search.model.FilterOperator;

/**
 * DTO REST - Enum pour les opérateurs de filtrage
 * Abstraction de l'enum du domaine pour la couche API REST
 * Sérialisé en camelCase dans les réponses JSON
 */
public enum FilterOperatorResponse {
    LESS_THAN("lessThan"),
    GREATER_THAN("greaterThan"),
    EQUALS("equals"),
    NOT_EQUALS("notEquals"),
    CONTAINS("contains"),
    NOT_CONTAINS("notContains"),
    IN("in"),
    NOT_IN("notIn"),
    BETWEEN("between"),
    STARTS_WITH("startsWith"),
    ENDS_WITH("endsWith"),
    BLANK("blank"),
    NOT_BLANK("notBlank");

    private final String value;

    FilterOperatorResponse(String value) {
        this.value = value;
    }

    /**
     * Utilisé par Jackson pour la sérialisation JSON (en camelCase)
     */
    @JsonValue
    public String getValue() {
        return value;
    }

    /**
     * Convertit le DTO vers l'enum du domaine
     */
    public FilterOperator toDomain() {
        return FilterOperator.valueOf(this.name());
    }

    /**
     * Crée un DTO depuis l'enum du domaine
     */
    public static FilterOperatorResponse fromDomain(FilterOperator operator) {
        return FilterOperatorResponse.valueOf(operator.name());
    }
}
