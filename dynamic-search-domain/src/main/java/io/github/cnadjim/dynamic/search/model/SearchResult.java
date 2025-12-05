package io.github.cnadjim.dynamic.search.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Résultat paginé - Objet de valeur du domaine
 * Représentation générique d'un résultat paginé sans dépendance à Spring Data
 *
 * @param content       Liste des éléments de la number courante
 * @param pageNumber    Numéro de la number (commence à 0)
 * @param pageSize      Taille de la number
 * @param totalElements Nombre total d'éléments
 * @param totalPages    Nombre total de pages
 * @param sorts         Liste des critères de tri appliqués
 * @param first         Indique si c'est la première number
 * @param last          Indique si c'est la dernière number
 * @param empty         Indique si la number est vide
 */
public record SearchResult<T>(
        List<T> content,
        int pageNumber,
        int pageSize,
        long totalElements,
        int totalPages,
        List<SortCriteria> sorts,
        boolean first,
        boolean last,
        boolean empty
) implements Serializable {

    /**
     * Crée un builder pour construire un SearchResult
     */
    public static <T> Builder<T> builder() {
        return new Builder<>();
    }

    /**
     * Builder pour SearchResult
     */
    public static class Builder<T> {
        private List<T> content;
        private int pageNumber;
        private int pageSize;
        private long totalElements;
        private int totalPages;
        private List<SortCriteria> sorts;
        private boolean first;
        private boolean last;
        private boolean empty;

        public Builder<T> content(List<T> content) {
            this.content = content;
            return this;
        }

        public Builder<T> pageNumber(int pageNumber) {
            this.pageNumber = pageNumber;
            return this;
        }

        public Builder<T> pageSize(int pageSize) {
            this.pageSize = pageSize;
            return this;
        }

        public Builder<T> totalElements(long totalElements) {
            this.totalElements = totalElements;
            return this;
        }

        public Builder<T> totalPages(int totalPages) {
            this.totalPages = totalPages;
            return this;
        }

        public Builder<T> sorts(List<SortCriteria> sorts) {
            this.sorts = sorts;
            return this;
        }

        public Builder<T> first(boolean first) {
            this.first = first;
            return this;
        }

        public Builder<T> last(boolean last) {
            this.last = last;
            return this;
        }

        public Builder<T> empty(boolean empty) {
            this.empty = empty;
            return this;
        }

        public SearchResult<T> build() {
            return new SearchResult<>(content, pageNumber, pageSize, totalElements, totalPages, sorts, first, last, empty);
        }
    }

}
