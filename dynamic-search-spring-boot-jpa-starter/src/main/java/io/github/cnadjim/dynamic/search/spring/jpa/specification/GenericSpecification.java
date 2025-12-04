package io.github.cnadjim.dynamic.search.spring.jpa.specification;

import io.github.cnadjim.dynamic.search.model.*;
import io.github.cnadjim.dynamic.search.spring.starter.util.FieldTypeParser;
import jakarta.persistence.criteria.*;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Spécification JPA générique - Construction des requêtes dynamiques
 * Détail d'implémentation qui traduit les critères du domaine en requêtes JPA
 * Fonctionne avec n'importe quel type d'entité JPA
 *
 * @param <E> Type de l'entité JPA
 */
@Slf4j
@AllArgsConstructor
public class GenericSpecification<E> implements Specification<E> {

    private static final long serialVersionUID = -9153865343320750644L;

    private final SearchCriteria criteria;

    @Override
    public Predicate toPredicate(@NonNull Root<E> root, @Nullable CriteriaQuery<?> query, CriteriaBuilder cb) {
        Predicate predicate = cb.equal(cb.literal(Boolean.TRUE), Boolean.TRUE);

        // Application des filtres
        for (FilterCriteria filter : criteria.filters()) {
            log.info("Filter: {} {} {}", filter.key(), filter.operator(), filter.value());
            predicate = buildPredicate(root, cb, filter, predicate);
        }

        // Application des tris
        List<Order> orders = new ArrayList<>();

        for (SortCriteria sort : criteria.sorts()) {
            orders.add(buildOrder(root, cb, sort));
        }

        query.orderBy(orders);

        return predicate;
    }

    private Predicate buildPredicate(Root<E> root, CriteriaBuilder cb,
                                     FilterCriteria filter, Predicate predicate) {
        return switch (filter.operator()) {
            case EQUALS -> buildEquals(root, cb, filter, predicate);
            case NOT_EQUALS -> buildNotEquals(root, cb, filter, predicate);
            case LESS_THAN -> buildLessThan(root, cb, filter, predicate);
            case GREATER_THAN -> buildGreaterThan(root, cb, filter, predicate);
            case CONTAINS -> buildContains(root, cb, filter, predicate);
            case NOT_CONTAINS -> buildNotContains(root, cb, filter, predicate);
            case IN -> buildIn(root, cb, filter, predicate);
            case NOT_IN -> buildNotIn(root, cb, filter, predicate);
            case BETWEEN -> buildBetween(root, cb, filter, predicate);
            case STARTS_WITH -> buildStartsWith(root, cb, filter, predicate);
            case ENDS_WITH -> buildEndsWith(root, cb, filter, predicate);
            case BLANK -> buildBlank(root, cb, filter, predicate);
            case NOT_BLANK -> buildNotBlank(root, cb, filter, predicate);
        };
    }

    private Predicate buildEquals(Root<E> root, CriteriaBuilder cb,
                                  FilterCriteria filter, Predicate predicate) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());

        // Si c'est un champ DATE et que la valeur fournie est une date sans heure (format yyyy-MM-dd),
        // on transforme automatiquement en BETWEEN pour matcher toute la journée
        if (filter.fieldType() == FieldType.DATE && isDateWithoutTime(filter.value().toString())) {
            return buildDateRangeForWholeDay(root, cb, filter, predicate, (LocalDateTime) value);
        }

        Expression<?> key = root.get(filter.key());
        return cb.and(cb.equal(key, value), predicate);
    }

    /**
     * Vérifie si la chaîne de date est au format date seule (yyyy-MM-dd) sans heure
     */
    private boolean isDateWithoutTime(String dateString) {
        // Format date seule : yyyy-MM-dd (10 caractères)
        // Format avec heure : yyyy-MM-ddTHH:mm:ss (au moins 19 caractères)
        return dateString != null && dateString.length() == 10 && dateString.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Crée un prédicat BETWEEN pour matcher toute la journée (de 00:00:00 à 23:59:59.999999999)
     */
    private Predicate buildDateRangeForWholeDay(Root<E> root, CriteriaBuilder cb,
                                                 FilterCriteria filter, Predicate predicate,
                                                 LocalDateTime startOfDay) {
        Expression<LocalDateTime> dateKey = root.get(filter.key());
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1); // 23:59:59.999999999

        log.debug("Converting date EQUALS to BETWEEN range: {} - {}", startOfDay, endOfDay);

        return cb.and(
                cb.and(
                        cb.greaterThanOrEqualTo(dateKey, startOfDay),
                        cb.lessThanOrEqualTo(dateKey, endOfDay)
                ),
                predicate
        );
    }

    private Predicate buildNotEquals(Root<E> root, CriteriaBuilder cb,
                                     FilterCriteria filter,
                                     Predicate predicate) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        Expression<?> key = root.get(filter.key());
        return cb.and(cb.notEqual(key, value), predicate);
    }

    private Predicate buildLessThan(Root<E> root, CriteriaBuilder cb,
                                    FilterCriteria filter,
                                    Predicate predicate) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());

        return switch (filter.fieldType()) {
            case DATE -> {
                Expression<LocalDateTime> dateKey = root.get(filter.key());
                yield cb.and(cb.lessThan(dateKey, (LocalDateTime) value), predicate);
            }
            case NUMBER -> {
                Expression<Number> numKey = root.get(filter.key());
                yield cb.and(cb.lt(numKey, (Number) value), predicate);
            }
            default -> {
                log.warn("LESS_THAN operator not supported for {} field type", filter.fieldType());
                yield predicate;
            }
        };
    }

    private Predicate buildGreaterThan(Root<E> root, CriteriaBuilder cb,
                                       FilterCriteria filter, Predicate predicate) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());

        return switch (filter.fieldType()) {
            case DATE -> {
                Expression<LocalDateTime> dateKey = root.get(filter.key());
                yield cb.and(cb.greaterThan(dateKey, (LocalDateTime) value), predicate);
            }
            case NUMBER -> {
                Expression<Number> numKey = root.get(filter.key());
                yield cb.and(cb.gt(numKey, (Number) value), predicate);
            }
            default -> {
                log.warn("GREATER_THAN operator not supported for {} field type", filter.fieldType());
                yield predicate;
            }
        };
    }

    private Predicate buildContains(Root<E> root, CriteriaBuilder cb,
                                    FilterCriteria filter, Predicate predicate) {
        Expression<String> key = root.get(filter.key());
        return cb.and(cb.like(cb.upper(key), "%" + filter.value().toString().toUpperCase() + "%"), predicate);
    }

    private Predicate buildNotContains(Root<E> root, CriteriaBuilder cb,
                                       FilterCriteria filter, Predicate predicate) {
        Expression<String> key = root.get(filter.key());
        return cb.and(cb.notLike(cb.upper(key), "%" + filter.value().toString().toUpperCase() + "%"), predicate);
    }

    private Predicate buildStartsWith(Root<E> root, CriteriaBuilder cb,
                                      FilterCriteria filter, Predicate predicate) {
        Expression<String> key = root.get(filter.key());
        return cb.and(cb.like(cb.upper(key), filter.value().toString().toUpperCase() + "%"), predicate);
    }

    private Predicate buildEndsWith(Root<E> root, CriteriaBuilder cb,
                                    FilterCriteria filter, Predicate predicate) {
        Expression<String> key = root.get(filter.key());
        return cb.and(cb.like(cb.upper(key), "%" + filter.value().toString().toUpperCase()), predicate);
    }

    private Predicate buildBlank(Root<E> root, CriteriaBuilder cb,
                                 FilterCriteria filter, Predicate predicate) {
        Expression<?> key = root.get(filter.key());
        Predicate isNull = cb.isNull(key);
        Predicate isEmpty = cb.equal(cb.length((Expression<String>) key), 0);
        return cb.and(cb.or(isNull, isEmpty), predicate);
    }

    private Predicate buildNotBlank(Root<E> root, CriteriaBuilder cb,
                                    FilterCriteria filter, Predicate predicate) {
        Expression<?> key = root.get(filter.key());
        Predicate isNotNull = cb.isNotNull(key);
        Predicate isNotEmpty = cb.greaterThan(cb.length((Expression<String>) key), 0);
        return cb.and(cb.and(isNotNull, isNotEmpty), predicate);
    }

    private Predicate buildIn(Root<E> root, CriteriaBuilder cb,
                              FilterCriteria filter, Predicate predicate) {
        List<Object> values = filter.values();
        CriteriaBuilder.In<Object> inClause = cb.in(root.get(filter.key()));
        for (Object value : values) {
            inClause.value(FieldTypeParser.parse(filter.fieldType(), value.toString()));
        }
        return cb.and(inClause, predicate);
    }

    private Predicate buildNotIn(Root<E> root, CriteriaBuilder cb,
                                 FilterCriteria filter, Predicate predicate) {
        List<Object> values = filter.values();
        CriteriaBuilder.In<Object> inClause = cb.in(root.get(filter.key()));
        for (Object value : values) {
            inClause.value(FieldTypeParser.parse(filter.fieldType(), value.toString()));
        }
        return cb.and(cb.not(inClause), predicate);
    }

    private Predicate buildBetween(Root<E> root, CriteriaBuilder cb,
                                   FilterCriteria filter, Predicate predicate) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        Object valueTo = FieldTypeParser.parse(filter.fieldType(), filter.valueTo().toString());

        switch (filter.fieldType()) {
            case DATE:
                LocalDateTime startDate = (LocalDateTime) value;
                LocalDateTime endDate = (LocalDateTime) valueTo;
                Expression<LocalDateTime> dateKey = root.get(filter.key());
                return cb.and(cb.and(cb.greaterThanOrEqualTo(dateKey, startDate),
                        cb.lessThanOrEqualTo(dateKey, endDate)), predicate);

            case NUMBER:
                Number start = (Number) value;
                Number end = (Number) valueTo;
                Expression<Number> numKey = root.get(filter.key());
                return cb.and(cb.and(cb.ge(numKey, start), cb.le(numKey, end)), predicate);

            default:
                log.info("Cannot use BETWEEN for {} field type.", filter.fieldType());
                return predicate;
        }
    }

    private Order buildOrder(Root<E> root, CriteriaBuilder cb, SortCriteria sort) {
        if (SortDirection.DESC.equals(sort.direction())) {
            return cb.desc(root.get(sort.key()));
        } else {
            return cb.asc(root.get(sort.key()));
        }
    }

}
