package io.github.cnadjim.dynamic.search.spring.elasticsearch.criteria;

import co.elastic.clients.elasticsearch._types.FieldValue;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import io.github.cnadjim.dynamic.search.model.FilterCriteria;
import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.spring.starter.util.FieldTypeParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;

import java.util.List;

/**
 * Constructeur de critères Elasticsearch - Construction des requêtes dynamiques
 * Traduit les critères du domaine en requêtes Elasticsearch
 */
@Slf4j
public final class ElasticsearchCriteriaBuilder {

    private ElasticsearchCriteriaBuilder() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Construit une Query Elasticsearch à partir des critères du domaine
     */
    public static NativeQuery buildQuery(SearchCriteria searchCriteria) {
        if (searchCriteria.filters().isEmpty()) {
            return NativeQuery.builder().build();
        }

        BoolQuery.Builder boolQueryBuilder = new BoolQuery.Builder();

        // Application des filtres
        for (FilterCriteria filter : searchCriteria.filters()) {
            log.info("Filter: {} {} {}", filter.key(), filter.operator(), filter.value());
            Query query = buildCriteria(filter);
            if (query != null) {
                boolQueryBuilder.must(query);
            }
        }

        return NativeQuery.builder()
                .withQuery(boolQueryBuilder.build()._toQuery())
                .build();
    }

    /**
     * Construit une Query Elasticsearch à partir d'un FilterCriteria
     */
    private static Query buildCriteria(FilterCriteria filter) {
        return switch (filter.operator()) {
            case EQUALS -> buildEquals(filter);
            case NOT_EQUALS -> buildNotEquals(filter);
            case LESS_THAN -> buildLessThan(filter);
            case GREATER_THAN -> buildGreaterThan(filter);
            case CONTAINS -> buildContains(filter);
            case NOT_CONTAINS -> buildNotContains(filter);
            case IN -> buildIn(filter);
            case NOT_IN -> buildNotIn(filter);
            case BETWEEN -> buildBetween(filter);
            case STARTS_WITH -> buildStartsWith(filter);
            case ENDS_WITH -> buildEndsWith(filter);
            case BLANK -> buildBlank(filter);
            case NOT_BLANK -> buildNotBlank(filter);
        };
    }

    private static Query buildEquals(FilterCriteria filter) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        return Query.of(q -> q.term(t -> t.field(filter.key()).value(toFieldValue(value))));
    }

    private static Query buildNotEquals(FilterCriteria filter) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        return Query.of(q -> q.bool(b -> b.mustNot(
                Query.of(qq -> qq.term(t -> t.field(filter.key()).value(toFieldValue(value))))
        )));
    }

    private static Query buildLessThan(FilterCriteria filter) {
        String value = filter.value().toString();
        return Query.of(q -> q.range(r -> {
            var builder = co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery.of(nr -> nr
                    .field(filter.key())
                    .lt(Double.parseDouble(value))
            );
            return r.number(builder);
        }));
    }

    private static Query buildGreaterThan(FilterCriteria filter) {
        String value = filter.value().toString();
        return Query.of(q -> q.range(r -> {
            var builder = co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery.of(nr -> nr
                    .field(filter.key())
                    .gt(Double.parseDouble(value))
            );
            return r.number(builder);
        }));
    }

    private static Query buildContains(FilterCriteria filter) {
        // Elasticsearch wildcard query pour recherche case insensitive
        String wildcardValue = "*" + filter.value().toString().toLowerCase() + "*";
        return Query.of(q -> q.wildcard(w -> w.field(filter.key()).value(wildcardValue).caseInsensitive(true)));
    }

    private static Query buildNotContains(FilterCriteria filter) {
        String wildcardValue = "*" + filter.value().toString().toLowerCase() + "*";
        return Query.of(q -> q.bool(b -> b.mustNot(
                Query.of(qq -> qq.wildcard(w -> w.field(filter.key()).value(wildcardValue).caseInsensitive(true)))
        )));
    }

    private static Query buildStartsWith(FilterCriteria filter) {
        String wildcardValue = filter.value().toString().toLowerCase() + "*";
        return Query.of(q -> q.wildcard(w -> w.field(filter.key()).value(wildcardValue).caseInsensitive(true)));
    }

    private static Query buildEndsWith(FilterCriteria filter) {
        String wildcardValue = "*" + filter.value().toString().toLowerCase();
        return Query.of(q -> q.wildcard(w -> w.field(filter.key()).value(wildcardValue).caseInsensitive(true)));
    }

    private static Query buildIn(FilterCriteria filter) {
        List<FieldValue> values = filter.values().stream()
                .map(value -> {
                    Object parsed = FieldTypeParser.parse(filter.fieldType(), value.toString());
                    return toFieldValue(parsed);
                })
                .toList();
        return Query.of(q -> q.terms(t -> t.field(filter.key()).terms(tf -> tf.value(values))));
    }

    private static Query buildNotIn(FilterCriteria filter) {
        List<FieldValue> values = filter.values().stream()
                .map(value -> {
                    Object parsed = FieldTypeParser.parse(filter.fieldType(), value.toString());
                    return toFieldValue(parsed);
                })
                .toList();
        return Query.of(q -> q.bool(b -> b.mustNot(
                Query.of(qq -> qq.terms(t -> t.field(filter.key()).terms(tf -> tf.value(values))))
        )));
    }

    private static Query buildBetween(FilterCriteria filter) {
        String start = filter.value().toString();
        String end = filter.valueTo().toString();
        return Query.of(q -> q.range(r -> {
            var builder = co.elastic.clients.elasticsearch._types.query_dsl.NumberRangeQuery.of(nr -> nr
                    .field(filter.key())
                    .gte(Double.parseDouble(start))
                    .lte(Double.parseDouble(end))
            );
            return r.number(builder);
        }));
    }

    private static Query buildBlank(FilterCriteria filter) {
        // Champ qui n'existe pas ou qui est vide
        return Query.of(q -> q.bool(b -> b
                .should(Query.of(sq -> sq.bool(bb -> bb.mustNot(Query.of(qq -> qq.exists(e -> e.field(filter.key())))))))
                .should(Query.of(sq -> sq.term(t -> t.field(filter.key()).value(FieldValue.of("")))))
                .minimumShouldMatch("1")
        ));
    }

    private static Query buildNotBlank(FilterCriteria filter) {
        // Champ qui existe et qui n'est pas vide
        return Query.of(q -> q.bool(b -> b
                .must(Query.of(qq -> qq.exists(e -> e.field(filter.key()))))
                .mustNot(Query.of(qq -> qq.term(t -> t.field(filter.key()).value(FieldValue.of("")))))
        ));
    }

    /**
     * Convertit un Object en FieldValue pour Elasticsearch
     */
    private static FieldValue toFieldValue(Object value) {
        if (value == null) {
            return FieldValue.NULL;
        }
        if (value instanceof String) {
            return FieldValue.of((String) value);
        }
        if (value instanceof Long) {
            return FieldValue.of((Long) value);
        }
        if (value instanceof Integer) {
            return FieldValue.of(((Integer) value).longValue());
        }
        if (value instanceof Double) {
            return FieldValue.of((Double) value);
        }
        if (value instanceof Float) {
            return FieldValue.of(((Float) value).doubleValue());
        }
        if (value instanceof Boolean) {
            return FieldValue.of((Boolean) value);
        }
        // Par défaut, convertir en String
        return FieldValue.of(value.toString());
    }

}
