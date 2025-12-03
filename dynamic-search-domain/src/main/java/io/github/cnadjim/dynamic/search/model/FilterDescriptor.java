package io.github.cnadjim.dynamic.search.model;

import java.io.Serializable;
import java.util.Set;

/**
 * Descripteur de filtre disponible pour une entité
 * Utilisé pour la découverte dynamique des champs filtrables
 *
 * @param key Nom du champ (ex: "name", "price", "createdAt")
 * @param fieldType Type du champ (STRING, NUMBER, DATE, BOOLEAN)
 * @param nullable Indique si le champ peut être null
 * @param availableOperators Liste des opérateurs disponibles pour ce champ
 */
public record FilterDescriptor(
        String key,
        FieldType fieldType,
        boolean nullable,
        Set<FilterOperator> availableOperators
) implements Serializable {

    public FilterDescriptor {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Filter key cannot be null or blank");
        }

        if (fieldType == null) {
            throw new IllegalArgumentException("Field type cannot be null");
        }

        if (availableOperators == null || availableOperators.isEmpty()) {
            throw new IllegalArgumentException("Available operators cannot be null or empty");
        }
    }
}
