package io.github.cnadjim.dynamic.search.model;

import java.io.Serializable;

/**
 * Critère de tri - Objet de valeur du domaine
 *
 * @param key Nom du champ sur lequel trier
 * @param direction Direction du tri (ASC ou DESC)
 */
public record SortCriteria(
        String key,
        SortDirection direction
) implements Serializable {

    /**
     * Builder pour SortCriteria
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String key;
        private SortDirection direction;

        public Builder key(String key) {
            this.key = key;
            return this;
        }

        public Builder direction(SortDirection direction) {
            this.direction = direction;
            return this;
        }

        public SortCriteria build() {
            return new SortCriteria(key, direction);
        }
    }

}
