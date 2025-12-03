package io.github.cnadjim.dynamic.search.spring.starter.mapper;

import io.github.cnadjim.dynamic.search.model.*;
import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;
import io.github.cnadjim.dynamic.search.spring.starter.request.FilterRequest;
import io.github.cnadjim.dynamic.search.spring.starter.request.PageRequest;
import io.github.cnadjim.dynamic.search.spring.starter.request.SearchRequest;
import io.github.cnadjim.dynamic.search.spring.starter.request.SortRequest;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper REST - Convertit les DTOs REST en objets du domaine
 * Responsable de la traduction des contrats externes vers le domaine
 */
public final class SearchRequestMapper {

    private SearchRequestMapper() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Convertit une SearchRequest REST en SearchCriteria du domaine
     * Déduit automatiquement les fieldType depuis les métadonnées de l'entité
     *
     * @param request SearchRequest provenant du client
     * @param entityClass Classe de l'entité recherchée
     * @param metadataStorage Storage des métadonnées pour déduire les types de champs
     * @return SearchCriteria du domaine
     */
    public static SearchCriteria toDomain(SearchRequest request, Class<?> entityClass, EntityMetadataStorage metadataStorage) {
        if (request == null) {
            return SearchCriteria.builder().build();
        }

        List<FilterCriteria> filters = request.getFilters()
                .stream()
                .map(filter -> toFilterCriteria(filter, entityClass, metadataStorage))
                .collect(Collectors.toList());

        List<SortCriteria> sorts = request.getSorts()
                .stream()
                .map(SearchRequestMapper::toSortCriteria)
                .collect(Collectors.toList());

        // Extraction de page et size depuis le PageRequest
        PageRequest pageRequest = request.getPageRequest();
        PageCriteria pageCriteria = new PageCriteria(pageRequest.page(), pageRequest.size());

        return SearchCriteria.builder()
                .filters(filters)
                .sorts(sorts)
                .pageCriteria(pageCriteria)
                .build();
    }

    /**
     * Version legacy pour rétro-compatibilité (sans déduction automatique)
     * @deprecated Utiliser {@link #toDomain(SearchRequest, Class, EntityMetadataStorage)}
     */
    @Deprecated
    public static SearchCriteria toDomain(SearchRequest request) {
        if (request == null) {
            return SearchCriteria.builder().build();
        }

        List<FilterCriteria> filters = request.getFilters()
                .stream()
                .map(SearchRequestMapper::toFilterCriteriaLegacy)
                .collect(Collectors.toList());

        List<SortCriteria> sorts = request.getSorts()
                .stream()
                .map(SearchRequestMapper::toSortCriteria)
                .collect(Collectors.toList());

        PageRequest pageRequest = request.getPageRequest();
        PageCriteria pageCriteria = new PageCriteria(pageRequest.page(), pageRequest.size());

        return SearchCriteria.builder()
                .filters(filters)
                .sorts(sorts)
                .pageCriteria(pageCriteria)
                .build();
    }

    private static FilterCriteria toFilterCriteria(FilterRequest request, Class<?> entityClass, EntityMetadataStorage metadataStorage) {
        // Résoudre le fieldType (fourni ou déduit depuis les métadonnées)
        FieldType resolvedFieldType = request.fieldType() != null
                ? request.fieldType().toDomain()
                : metadataStorage.resolveFieldType(entityClass, request.key());

        return FilterCriteria.builder()
                .key(request.key())
                .operator(request.operator().toDomain())
                .fieldType(resolvedFieldType)
                .value(request.value())
                .valueTo(request.valueTo())
                .values(request.values())
                .build();
    }

    private static FilterCriteria toFilterCriteriaLegacy(FilterRequest request) {
        return FilterCriteria.builder()
                .key(request.key())
                .operator(request.operator().toDomain())
                .fieldType(request.fieldType() != null ? request.fieldType().toDomain() : FieldType.STRING)
                .value(request.value())
                .valueTo(request.valueTo())
                .values(request.values())
                .build();
    }

    private static SortCriteria toSortCriteria(SortRequest request) {
        return SortCriteria.builder()
                .key(request.key())
                .direction(request.direction().toDomain())  // Conversion DTO vers domaine
                .build();
    }

}
