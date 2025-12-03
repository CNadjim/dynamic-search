package io.github.cnadjim.dynamic.search.model;

import java.io.Serializable;
import java.util.List;

/**
 * Critère de filtrage - Objet de valeur du domaine
 *
 * @param key Nom du champ à filtrer
 * @param operator Opérateur de filtrage (EQUAL, LIKE, IN, etc.)
 * @param fieldType Type du champ (STRING, INTEGER, DATE, etc.)
 * @param value Valeur pour les opérateurs simples (EQUAL, LIKE, etc.)
 * @param valueTo Valeur de fin pour l'opérateur BETWEEN
 * @param values Liste de valeurs pour l'opérateur IN
 */
public record FilterCriteria(
        String key,
        FilterOperator operator,
        FieldType fieldType,
        Object value,
        Object valueTo,
        List<Object> values
) implements Serializable {

    /**
     * Builder pour FilterCriteria
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private FilterOperator operator;
        private FieldType fieldType;
        private Object value;
        private Object valueTo;
        private List<Object> values;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder operator(FilterOperator operator) {
            this.operator = operator;
            return this;
        }

        public Builder fieldType(FieldType fieldType) {
            this.fieldType = fieldType;
            return this;
        }

        public Builder value(Object value) {
            this.value = value;
            return this;
        }

        public Builder valueTo(Object valueTo) {
            this.valueTo = valueTo;
            return this;
        }

        public Builder values(List<Object> values) {
            this.values = values;
            return this;
        }

        public FilterCriteria build() {
            return new FilterCriteria(key, operator, fieldType, value, valueTo, values);
        }
    }

}
