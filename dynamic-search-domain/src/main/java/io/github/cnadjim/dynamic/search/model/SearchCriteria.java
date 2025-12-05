package io.github.cnadjim.dynamic.search.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

/**
 * Critères de recherche - Objet de valeur du domaine
 * Représente les critères de filtrage, tri, recherche full-text et pagination pour une recherche
 *
 * @param filters Liste des filtres (peut être null, retournée comme liste vide)
 * @param sorts Liste des tris (peut être null, retournée comme liste vide)
 * @param fullText Critère de recherche full-text (peut être null)
 * @param page Critères de pagination (number et taille)
 */
public record SearchCriteria(
        List<FilterCriteria> filters,
        List<SortCriteria> sorts,
        FullTextCriteria fullText,
        PageCriteria page
) implements Serializable {

    /**
     * Retourne la liste des filtres, jamais null
     */
    public List<FilterCriteria> filters() {
        return Objects.isNull(filters) ? new ArrayList<>() : filters;
    }

    /**
     * Retourne la liste des tris, jamais null
     */
    public List<SortCriteria> sorts() {
        return Objects.isNull(sorts) ? new ArrayList<>() : sorts;
    }

    /**
     * Retourne les critères de pagination, jamais null (valeurs par défaut : number=0, size=100)
     */
    public PageCriteria page() {
        return Objects.isNull(page) ? new PageCriteria(0, 100) : page;
    }

    /**
     * Retourne le critère de recherche full-text, peut être null
     */
    public FullTextCriteria fullText() {
        return fullText;
    }

    /**
     * Vérifie si une recherche full-text est active
     */
    public boolean hasFullTextSearch() {
        return fullText != null && fullText.query() != null && !fullText.query().isBlank();
    }

    /**
     * Builder pour SearchCriteria
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private List<FilterCriteria> filters;
        private List<SortCriteria> sorts;
        private FullTextCriteria fullTextCriteria;
        private PageCriteria pageCriteria;

        public Builder filters(List<FilterCriteria> filters) {
            this.filters = filters;
            return this;
        }

        public Builder sorts(List<SortCriteria> sorts) {
            this.sorts = sorts;
            return this;
        }

        public Builder fullTextCriteria(FullTextCriteria fullTextCriteria) {
            this.fullTextCriteria = fullTextCriteria;
            return this;
        }

        public Builder pageCriteria(PageCriteria pageCriteria) {
            this.pageCriteria = pageCriteria;
            return this;
        }

        public SearchCriteria build() {
            return new SearchCriteria(filters, sorts, fullTextCriteria, pageCriteria);
        }
    }

}
