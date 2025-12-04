package io.github.cnadjim.dynamic.search.spring.starter.mapper;

import io.github.cnadjim.dynamic.search.model.*;
import io.github.cnadjim.dynamic.search.port.in.GetFieldTypeUseCase;
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
     * Déduit automatiquement les fieldType via le use case GetFieldTypeUseCase
     *
     * @param request SearchRequest provenant du client
     * @param entityClass Classe de l'entité recherchée
     * @param getFieldTypeUseCase Use case pour déduire les types de champs
     * @return SearchCriteria du domaine
     */
    public static SearchCriteria toDomain(SearchRequest request, Class<?> entityClass, GetFieldTypeUseCase getFieldTypeUseCase) {
        if (request == null) {
            return SearchCriteria.builder().build();
        }

        List<FilterCriteria> filters = request.getFilters()
                .stream()
                .map(filter -> toFilterCriteria(filter, entityClass, getFieldTypeUseCase))
                .collect(Collectors.toList());

        List<SortCriteria> sorts = request.getSorts()
                .stream()
                .map(SearchRequestMapper::toSortCriteria)
                .collect(Collectors.toList());

        PageCriteria pageCriteria = toDomain(request.getPage());

        return SearchCriteria.builder()
                .filters(filters)
                .sorts(sorts)
                .pageCriteria(pageCriteria)
                .build();
    }


    private static PageCriteria toDomain(PageRequest request) {
        return new PageCriteria(request.page(), request.size());
    }

    private static FilterCriteria toFilterCriteria(FilterRequest request, Class<?> entityClass, GetFieldTypeUseCase getFieldTypeUseCase) {
        // Résolution du type de champ via le use case
        FieldType resolvedFieldType;
        try {
            resolvedFieldType = getFieldTypeUseCase.getFieldType(request.key(), entityClass);
        } catch (Exception e) {
            // Fallback sur STRING si le champ n'est pas trouvé
            resolvedFieldType = FieldType.STRING;
        }

        return FilterCriteria.builder()
                .key(request.key())
                .operator(request.operator().toDomain())
                .fieldType(resolvedFieldType)
                .value(request.value())
                .valueTo(request.valueTo())
                .values(request.values())
                .build();
    }

    private static SortCriteria toSortCriteria(SortRequest request) {
        return SortCriteria.builder()
                .key(request.key())
                .direction(request.direction().toDomain())
                .build();
    }

}
