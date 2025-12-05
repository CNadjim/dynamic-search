package io.github.cnadjim.dynamic.search.model;

import java.io.Serializable;

/**
 * Critère de recherche full-text - Objet de valeur du domaine
 * Permet de faire des recherches rapides sur l'intégralité des champs searchable d'une entité
 *
 * @param query Texte de recherche à chercher dans tous les champs searchable
 */
public record FullTextCriteria(
        String query
) implements Serializable {

    /**
     * Builder pour FullTextCriteria
     */
    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String query;

        public Builder query(String query) {
            this.query = query;
            return this;
        }

        public FullTextCriteria build() {
            return new FullTextCriteria(query);
        }
    }

}
