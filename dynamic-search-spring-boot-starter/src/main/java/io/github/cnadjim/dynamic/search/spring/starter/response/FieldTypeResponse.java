package io.github.cnadjim.dynamic.search.spring.starter.response;

import com.fasterxml.jackson.annotation.JsonValue;
import io.github.cnadjim.dynamic.search.model.FieldType;

/**
 * DTO REST - Enum pour les types de champs
 * Abstraction de l'enum du domaine pour la couche API REST
 * Sérialisé en lowercase dans les réponses JSON
 */
public enum FieldTypeResponse {
    STRING("string"),
    NUMBER("number"),
    DATE("date"),
    BOOLEAN("boolean");

    private final String value;

    FieldTypeResponse(String value) {
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
    public FieldType toDomain() {
        return FieldType.valueOf(this.name());
    }

    /**
     * Crée un DTO depuis l'enum du domaine
     */
    public static FieldTypeResponse fromDomain(FieldType fieldType) {
        return FieldTypeResponse.valueOf(fieldType.name());
    }
}
