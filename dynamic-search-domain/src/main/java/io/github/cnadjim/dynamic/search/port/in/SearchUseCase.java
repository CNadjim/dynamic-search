package io.github.cnadjim.dynamic.search.port.in;

import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;

/**
 * Port IN - Cas d'usage générique de recherche
 * Interface définie dans le domaine pour rechercher n'importe quel type d'agrégat
 *
 * @param <T> Type de l'agrégat à rechercher
 */
public interface SearchUseCase<T> {

    /**
     * Recherche des agrégats selon des critères
     *
     * @param criteria critères de recherche (filtres, tris, pagination)
     * @return résultat paginé contenant les agrégats trouvés
     */
    SearchResult<T> search(SearchCriteria criteria);

}
