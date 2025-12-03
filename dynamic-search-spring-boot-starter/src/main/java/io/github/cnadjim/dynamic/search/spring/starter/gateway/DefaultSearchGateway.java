package io.github.cnadjim.dynamic.search.spring.starter.gateway;

import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.port.in.GetAvailableFiltersUseCase;
import io.github.cnadjim.dynamic.search.port.in.SearchUseCase;
import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;
import io.github.cnadjim.dynamic.search.spring.starter.mapper.SearchRequestMapper;
import io.github.cnadjim.dynamic.search.spring.starter.request.SearchRequest;
import io.github.cnadjim.dynamic.search.spring.starter.response.FilterDescriptorResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationContext;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Implémentation par défaut du SearchGateway
 * Utilise le Spring ApplicationContext pour résoudre dynamiquement les use cases
 *
 * Cette implémentation :
 * - Résout les beans SearchUseCase<T> et GetAvailableFiltersUseCase<T> par type
 * - Convertit automatiquement les DTOs REST en objets du domaine
 * - Déduit automatiquement les fieldType depuis les métadonnées de l'entité
 * - Convertit les résultats du domaine en DTOs REST
 */
@Slf4j
public class DefaultSearchGateway implements SearchGateway {

    private final ApplicationContext applicationContext;
    private final EntityMetadataStorage metadataStorage;

    public DefaultSearchGateway(ApplicationContext applicationContext, EntityMetadataStorage metadataStorage) {
        if (applicationContext == null) {
            throw new IllegalArgumentException("ApplicationContext cannot be null");
        }
        if (metadataStorage == null) {
            throw new IllegalArgumentException("EntityMetadataStorage cannot be null");
        }
        this.applicationContext = applicationContext;
        this.metadataStorage = metadataStorage;
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

        // Résolution du SearchUseCase pour cette entité
        SearchUseCase<T> searchUseCase = resolveSearchUseCase(entityClass);

        // Conversion de la requête REST en critères du domaine avec déduction automatique des fieldType
        SearchCriteria criteria = SearchRequestMapper.toDomain(request, entityClass, metadataStorage);

        // Exécution de la recherche
        SearchResult<T> result = searchUseCase.search(criteria);

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

        // Résolution du GetAvailableFiltersUseCase pour cette entité
        GetAvailableFiltersUseCase<T> filtersUseCase = resolveFiltersUseCase(entityClass);

        // Récupération et conversion des filtres disponibles
        List<FilterDescriptorResponse> filters = filtersUseCase.getAvailableFilters()
                .stream()
                .map(FilterDescriptorResponse::fromDomain)
                .collect(Collectors.toList());

        log.debug("Found {} available filters for {}", filters.size(), entityClass.getSimpleName());

        return filters;
    }

    /**
     * Résout le SearchUseCase pour une entité via le contexte Spring
     * Recherche un bean de type SearchUseCase<T> où T correspond à l'entité
     *
     * @param entityClass Classe de l'entité
     * @param <T> Type de l'entité
     * @return SearchUseCase configuré pour cette entité
     * @throws IllegalStateException si aucun bean n'est trouvé
     */
    @SuppressWarnings("unchecked")
    private <T> SearchUseCase<T> resolveSearchUseCase(Class<T> entityClass) {
        try {
            // Recherche d'un bean nommé selon la convention : <entityName>SearchUseCase
            String beanName = entityClass.getSimpleName() + "SearchUseCase";

            if (applicationContext.containsBean(beanName)) {
                log.debug("Resolved SearchUseCase by name: {}", beanName);
                return (SearchUseCase<T>) applicationContext.getBean(beanName);
            }

            // Fallback : recherche par type générique (peut retourner le premier trouvé)
            log.debug("Searching SearchUseCase by type for {}", entityClass.getSimpleName());
            return (SearchUseCase<T>) applicationContext.getBean(SearchUseCase.class);

        } catch (Exception e) {
            String message = String.format(
                    "No SearchUseCase found for entity %s. " +
                    "Make sure the entity is properly annotated with @Entity (JPA) or @Document (MongoDB)",
                    entityClass.getSimpleName()
            );
            log.error(message, e);
            throw new IllegalStateException(message, e);
        }
    }

    /**
     * Résout le GetAvailableFiltersUseCase pour une entité via le contexte Spring
     *
     * @param entityClass Classe de l'entité
     * @param <T> Type de l'entité
     * @return GetAvailableFiltersUseCase configuré pour cette entité
     * @throws IllegalStateException si aucun bean n'est trouvé
     */
    @SuppressWarnings("unchecked")
    private <T> GetAvailableFiltersUseCase<T> resolveFiltersUseCase(Class<T> entityClass) {
        try {
            // Recherche d'un bean nommé selon la convention : <entityName>GetAvailableFiltersUseCase
            String beanName = entityClass.getSimpleName() + "GetAvailableFiltersUseCase";

            if (applicationContext.containsBean(beanName)) {
                log.debug("Resolved GetAvailableFiltersUseCase by name: {}", beanName);
                return (GetAvailableFiltersUseCase<T>) applicationContext.getBean(beanName);
            }

            // Fallback : recherche par type générique
            log.debug("Searching GetAvailableFiltersUseCase by type for {}", entityClass.getSimpleName());
            return (GetAvailableFiltersUseCase<T>) applicationContext.getBean(GetAvailableFiltersUseCase.class);

        } catch (Exception e) {
            String message = String.format(
                    "No GetAvailableFiltersUseCase found for entity %s. " +
                    "Make sure the entity is properly annotated with @Entity (JPA) or @Document (MongoDB)",
                    entityClass.getSimpleName()
            );
            log.error(message, e);
            throw new IllegalStateException(message, e);
        }
    }
}
