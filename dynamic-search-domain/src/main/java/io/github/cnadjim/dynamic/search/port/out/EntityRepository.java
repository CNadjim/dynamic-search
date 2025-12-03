package io.github.cnadjim.dynamic.search.port.out;

import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;

/**
 * Port OUT - Repository générique pour la persistance des agrégats
 * Interface définie dans le domaine, implémentée dans l'infrastructure
 * Permet au domaine de communiquer avec la persistance sans en dépendre
 *
 * Note: getAvailableFilters() a été déplacé vers FilterMetadataExtractor
 * car c'est une opération de réflexion sur le domaine, pas de persistance
 *
 * @param <T> Type de l'agrégat (entité métier)
 */
public interface EntityRepository<T> {

    /**
     * Recherche des agrégats selon des critères
     *
     * @param criteria critères de recherche
     * @return résultat paginé
     */
    SearchResult<T> findByCriteria(SearchCriteria criteria);

}
