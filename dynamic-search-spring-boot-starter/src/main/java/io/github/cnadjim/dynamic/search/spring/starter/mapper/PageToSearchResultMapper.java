package io.github.cnadjim.dynamic.search.spring.starter.mapper;

import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.model.SortCriteria;
import io.github.cnadjim.dynamic.search.model.SortDirection;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Sort;

import java.util.List;
import java.util.stream.Collectors;

/**
 * Mapper générique - Conversion de Spring Data Page vers SearchResult du domaine
 *
 * Cette classe est partagée entre les starters JPA et MongoDB pour éviter la duplication.
 * Fournie par le starter de base pour faciliter l'intégration.
 */
public final class PageToSearchResultMapper {

    private PageToSearchResultMapper() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Convertit une Page Spring Data en SearchResult du domaine
     *
     * @param page La number Spring Data à convertir
     * @param <T> Type du contenu
     * @return SearchResult contenant le même contenu que la number
     */
    public static <T> SearchResult<T> toSearchResult(Page<T> page) {
        if (page == null) {
            return SearchResult.<T>builder()
                    .content(List.of())
                    .pageNumber(0)
                    .pageSize(0)
                    .totalElements(0)
                    .totalPages(0)
                    .sorts(List.of())
                    .first(true)
                    .last(true)
                    .empty(true)
                    .build();
        }

        // Conversion des critères de tri
        List<SortCriteria> sorts = page.getSort()
                .stream()
                .map(PageToSearchResultMapper::toSortCriteria)
                .collect(Collectors.toList());

        return SearchResult.<T>builder()
                .content(page.getContent())
                .pageNumber(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .sorts(sorts)
                .first(page.isFirst())
                .last(page.isLast())
                .empty(page.isEmpty())
                .build();
    }

    /**
     * Convertit un Sort.Order Spring Data en SortCriteria du domaine
     */
    private static SortCriteria toSortCriteria(Sort.Order order) {
        return SortCriteria.builder()
                .key(order.getProperty())
                .direction(order.isAscending() ? SortDirection.ASC : SortDirection.DESC)
                .build();
    }

}
