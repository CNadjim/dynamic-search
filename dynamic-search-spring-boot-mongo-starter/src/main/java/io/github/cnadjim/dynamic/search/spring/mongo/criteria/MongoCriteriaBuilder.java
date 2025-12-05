package io.github.cnadjim.dynamic.search.spring.mongo.criteria;

import io.github.cnadjim.dynamic.search.metadata.FilterMetadataExtractor;
import io.github.cnadjim.dynamic.search.model.FieldType;
import io.github.cnadjim.dynamic.search.model.FilterCriteria;
import io.github.cnadjim.dynamic.search.model.FilterDescriptor;
import io.github.cnadjim.dynamic.search.model.SearchCriteria;
import io.github.cnadjim.dynamic.search.spring.starter.util.FieldTypeParser;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Constructeur de critères MongoDB - Construction des requêtes dynamiques
 * Traduit les critères du domaine en requêtes MongoDB
 */
@Slf4j
public final class MongoCriteriaBuilder {

    private MongoCriteriaBuilder() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Construit une Query MongoDB à partir des critères du domaine, incluant la recherche full-text
     * @param searchCriteria Critères de recherche
     * @param entityClass Classe de l'entité pour extraire les champs searchable
     */
    public static Query buildQuery(SearchCriteria searchCriteria, Class<?> entityClass) {
        Query query = new Query();

        // Application des filtres standards
        for (FilterCriteria filter : searchCriteria.filters()) {
            log.info("Filter: {} {} {}", filter.key(), filter.operator(), filter.value());
            Criteria criteria = buildCriteria(filter);
            if (criteria != null) {
                query.addCriteria(criteria);
            }
        }

        // Application de la recherche full-text si présente
        if (searchCriteria.hasFullTextSearch()) {
            log.info("Full-text search: {}", searchCriteria.fullText().query());
            Criteria fullTextCriteria = buildFullTextCriteria(searchCriteria.fullText().query(), entityClass);
            if (fullTextCriteria != null) {
                query.addCriteria(fullTextCriteria);
            }
        }

        return query;
    }

    /**
     * Construit un critère full-text qui cherche dans tous les champs STRING searchable
     * Utilise un OR entre tous les champs avec une recherche REGEX case-insensitive
     */
    private static Criteria buildFullTextCriteria(String searchQuery, Class<?> entityClass) {
        // Extraire les métadonnées des champs searchable
        List<FilterDescriptor> searchableFields = FilterMetadataExtractor.extractFilters(entityClass);

        // Filtrer uniquement les champs de type STRING (les seuls où on peut faire du full-text)
        List<String> stringFields = searchableFields.stream()
                .filter(field -> field.fieldType() == FieldType.STRING)
                .map(FilterDescriptor::key)
                .toList();

        if (stringFields.isEmpty()) {
            log.warn("No searchable STRING fields found for full-text search on entity: {}", entityClass.getSimpleName());
            return null;
        }

        // Construire un regex case-insensitive pour la recherche
        String regex = ".*" + searchQuery + ".*";

        // Créer un critère OR sur tous les champs STRING
        List<Criteria> fieldCriteria = new ArrayList<>();
        for (String fieldName : stringFields) {
            fieldCriteria.add(Criteria.where(fieldName).regex(regex, "i"));
        }

        log.debug("Full-text search on {} fields: {}", fieldCriteria.size(), stringFields);

        // Combiner tous les critères avec OR
        return new Criteria().orOperator(fieldCriteria.toArray(new Criteria[0]));
    }

    /**
     * Construit un Criteria MongoDB à partir d'un FilterCriteria
     */
    private static Criteria buildCriteria(FilterCriteria filter) {
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

    private static Criteria buildEquals(FilterCriteria filter) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());

        // Si c'est un champ DATE et que la valeur fournie est une date sans heure (format yyyy-MM-dd),
        // on transforme automatiquement en BETWEEN pour matcher toute la journée
        if (filter.fieldType() == FieldType.DATE && isDateWithoutTime(filter.value().toString())) {
            return buildDateRangeForWholeDay(filter, (LocalDateTime) value);
        }

        return Criteria.where(filter.key()).is(value);
    }

    /**
     * Vérifie si la chaîne de date est au format date seule (yyyy-MM-dd) sans heure
     */
    private static boolean isDateWithoutTime(String dateString) {
        // Format date seule : yyyy-MM-dd (10 caractères)
        // Format avec heure : yyyy-MM-ddTHH:mm:ss (au moins 19 caractères)
        return dateString != null && dateString.length() == 10 && dateString.matches("\\d{4}-\\d{2}-\\d{2}");
    }

    /**
     * Crée un critère BETWEEN pour matcher toute la journée (de 00:00:00 à 23:59:59.999999999)
     */
    private static Criteria buildDateRangeForWholeDay(FilterCriteria filter, LocalDateTime startOfDay) {
        LocalDateTime endOfDay = startOfDay.plusDays(1).minusNanos(1); // 23:59:59.999999999

        log.debug("Converting date EQUALS to BETWEEN range: {} - {}", startOfDay, endOfDay);

        return Criteria.where(filter.key())
                .gte(startOfDay)
                .lte(endOfDay);
    }

    private static Criteria buildNotEquals(FilterCriteria filter) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        return Criteria.where(filter.key()).ne(value);
    }

    private static Criteria buildLessThan(FilterCriteria filter) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        return Criteria.where(filter.key()).lt(value);
    }

    private static Criteria buildGreaterThan(FilterCriteria filter) {
        Object value = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        return Criteria.where(filter.key()).gt(value);
    }

    private static Criteria buildContains(FilterCriteria filter) {
        // MongoDB regex pour recherche case insensitive
        String regex = ".*" + filter.value().toString() + ".*";
        return Criteria.where(filter.key()).regex(regex, "i");
    }

    private static Criteria buildNotContains(FilterCriteria filter) {
        String regex = ".*" + filter.value().toString() + ".*";
        return Criteria.where(filter.key()).not().regex(regex, "i");
    }

    private static Criteria buildStartsWith(FilterCriteria filter) {
        String regex = "^" + filter.value().toString() + ".*";
        return Criteria.where(filter.key()).regex(regex, "i");
    }

    private static Criteria buildEndsWith(FilterCriteria filter) {
        String regex = ".*" + filter.value().toString() + "$";
        return Criteria.where(filter.key()).regex(regex, "i");
    }

    private static Criteria buildIn(FilterCriteria filter) {
        List<Object> values = filter.values().stream()
                .map(value -> FieldTypeParser.parse(filter.fieldType(), value.toString()))
                .toList();
        return Criteria.where(filter.key()).in(values);
    }

    private static Criteria buildNotIn(FilterCriteria filter) {
        List<Object> values = filter.values().stream()
                .map(value -> FieldTypeParser.parse(filter.fieldType(), value.toString()))
                .toList();
        return Criteria.where(filter.key()).nin(values);
    }

    private static Criteria buildBetween(FilterCriteria filter) {
        Object start = FieldTypeParser.parse(filter.fieldType(), filter.value().toString());
        Object end = FieldTypeParser.parse(filter.fieldType(), filter.valueTo().toString());
        return Criteria.where(filter.key()).gte(start).lte(end);
    }

    private static Criteria buildBlank(FilterCriteria filter) {
        // Champ null ou vide
        return new Criteria().orOperator(
                Criteria.where(filter.key()).is(null),
                Criteria.where(filter.key()).is("")
        );
    }

    private static Criteria buildNotBlank(FilterCriteria filter) {
        // Champ non null et non vide
        return new Criteria().andOperator(
                Criteria.where(filter.key()).ne(null),
                Criteria.where(filter.key()).ne("")
        );
    }

}
