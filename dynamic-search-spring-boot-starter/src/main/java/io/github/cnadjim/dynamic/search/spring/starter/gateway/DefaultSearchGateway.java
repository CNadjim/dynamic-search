package io.github.cnadjim.dynamic.search.spring.starter.gateway;

import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.port.in.GetAvailableFiltersUseCase;
import io.github.cnadjim.dynamic.search.port.in.GetFieldTypeUseCase;
import io.github.cnadjim.dynamic.search.port.in.SearchUseCase;
import io.github.cnadjim.dynamic.search.spring.starter.mapper.SearchRequestMapper;
import io.github.cnadjim.dynamic.search.spring.starter.request.SearchRequest;
import io.github.cnadjim.dynamic.search.spring.starter.response.FilterDescriptorResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation par défaut du SearchGateway
 * Utilise les use cases injectés pour effectuer les opérations de recherche
 *
 * Cette implémentation :
 * - Injecte directement les use cases nécessaires (SearchUseCase, GetAvailableFiltersUseCase, GetFieldTypeUseCase)
 * - Convertit automatiquement les DTOs REST en objets du domaine
 * - Déduit automatiquement les fieldType via GetFieldTypeUseCase
 * - Convertit les résultats du domaine en DTOs REST
 */
@Slf4j
public class DefaultSearchGateway implements SearchGateway {

    private final SearchUseCase searchUseCase;
    private final GetAvailableFiltersUseCase getAvailableFiltersUseCase;
    private final GetFieldTypeUseCase getFieldTypeUseCase;

    public DefaultSearchGateway(
            SearchUseCase searchUseCase,
            GetAvailableFiltersUseCase getAvailableFiltersUseCase,
            GetFieldTypeUseCase getFieldTypeUseCase) {
        if (searchUseCase == null) {
            throw new IllegalArgumentException("SearchUseCase cannot be null");
        }
        if (getAvailableFiltersUseCase == null) {
            throw new IllegalArgumentException("GetAvailableFiltersUseCase cannot be null");
        }
        if (getFieldTypeUseCase == null) {
            throw new IllegalArgumentException("GetFieldTypeUseCase cannot be null");
        }
        this.searchUseCase = searchUseCase;
        this.getAvailableFiltersUseCase = getAvailableFiltersUseCase;
        this.getFieldTypeUseCase = getFieldTypeUseCase;
    }

    @Override
    public <T> SearchResult<T> search(SearchRequest request, Class<T> entityClass) {
        // Validation des paramètres
        if (request == null) {
            throw new IllegalArgumentException("SearchRequest cannot be null");
        }
        if (entityClass == null) {
            throw new IllegalArgumentException("Entity class cannot be null");
        }

        log.debug("Searching for {} with {} filters and {} sorts",
                entityClass.getSimpleName(),
                request.getFilters().size(),
                request.getSorts().size());

        // Conversion de la requête REST en critères du domaine avec déduction automatique des fieldType
        SearchCriteria criteria = SearchRequestMapper.toDomain(request, entityClass, getFieldTypeUseCase);

        // Exécution de la recherche
        SearchResult<T> result = searchUseCase.search(criteria, entityClass);

        log.debug("Found {} results out of {} total for {}",
                result.content().size(),
                result.totalElements(),
                entityClass.getSimpleName());

        return result;
    }

    @Override
    public <T> List<FilterDescriptorResponse> getAvailableFilters(Class<T> entityClass) {
        // Validation des paramètres
        if (entityClass == null) {
            throw new IllegalArgumentException("Entity class cannot be null");
        }

        log.debug("Fetching available filters for {}", entityClass.getSimpleName());

        // Récupération et conversion des filtres disponibles
        List<FilterDescriptorResponse> filters = getAvailableFiltersUseCase.getAvailableFilters(entityClass)
                .stream()
                .map(FilterDescriptorResponse::fromDomain)
                .collect(Collectors.toList());

        log.debug("Found {} available filters for {}", filters.size(), entityClass.getSimpleName());

        return filters;
    }
}
