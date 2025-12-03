package io.github.cnadjim.dynamic.search.port.in;

import io.github.cnadjim.dynamic.search.model.FilterDescriptor;

import java.util.List;

/**
 * Port IN - Use case pour récupérer les filtres disponibles
 * Permet la découverte dynamique des champs filtrables d'une entité
 *
 * @param <T> Type de l'entité concernée
 */
public interface GetAvailableFiltersUseCase<T> {

    /**
     * Récupère la liste des filtres disponibles pour l'entité
     *
     * @return Liste des descripteurs de filtres (nom du champ + type)
     */
    List<FilterDescriptor> getAvailableFilters();

}
