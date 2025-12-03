package io.github.cnadjim.dynamic.search.metadata;

import io.github.cnadjim.dynamic.search.annotation.Searchable;
import io.github.cnadjim.dynamic.search.annotation.SearchableExclude;
import io.github.cnadjim.dynamic.search.model.FieldType;
import io.github.cnadjim.dynamic.search.model.FilterDescriptor;
import io.github.cnadjim.dynamic.search.model.FilterOperator;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Extracteur de métadonnées de filtres par réflexion
 * <p>
 * Stratégie d'extraction:
 * 1. Si un champ est annoté @Searchable : utilise les métadonnées explicites
 * 2. Si un champ est annoté @SearchableExclude : ignore le champ
 * 3. Sinon : auto-détecte le type et rend le champ searchable
 * <p>
 * Cette classe reste dans le domaine car c'est une opération métier de découverte
 */
public class FilterMetadataExtractor {

    /**
     * Extrait les descripteurs de filtres d'une classe annotée @EnableSearchable
     * <p>
     * Par défaut, tous les champs sont searchable sauf:
     * - Les champs annotés @SearchableExclude
     * - Les champs static ou transient
     * - Les collections et maps
     *
     * @param entityClass Classe à analyser
     * @return Liste des descripteurs de filtres disponibles
     */
    public static List<FilterDescriptor> extractFilters(Class<?> entityClass) {
        List<FilterDescriptor> filters = new ArrayList<>();

        // Parcourir tous les champs de la classe (incluant ceux hérités)
        getAllFields(entityClass).forEach(field -> {
            // Ignorer les champs exclus explicitement
            if (field.isAnnotationPresent(SearchableExclude.class)) {
                return;
            }

            // Ignorer les champs static, transient, final
            int modifiers = field.getModifiers();
            if (Modifier.isStatic(modifiers) || Modifier.isTransient(modifiers)) {
                return;
            }

            // Ignorer les collections et maps (trop complexe à filtrer)
            if (Collection.class.isAssignableFrom(field.getType()) ||
                    Map.class.isAssignableFrom(field.getType())) {
                return;
            }

            // Si le champ a @Searchable, utiliser les métadonnées explicites
            Searchable searchable = field.getAnnotation(Searchable.class);
            if (searchable != null) {
                String fieldName = searchable.fieldName().isEmpty()
                        ? field.getName()
                        : searchable.fieldName();

                FilterDescriptor descriptor = new FilterDescriptor(
                        fieldName,
                        searchable.type(),
                        searchable.nullable(),
                        getOperatorsForFieldType(searchable.type())
                );

                filters.add(descriptor);
            } else {
                // Sinon, auto-détection du type
                FieldType detectedType = detectFieldType(field.getType());
                if (detectedType != null) {
                    FilterDescriptor descriptor = new FilterDescriptor(
                            field.getName(),
                            detectedType,
                            true, // Par défaut, on considère les champs comme nullable
                            getOperatorsForFieldType(detectedType)
                    );

                    filters.add(descriptor);
                }
            }
        });

        return filters;
    }

    /**
     * Récupère tous les champs d'une classe, incluant ceux hérités
     */
    private static List<Field> getAllFields(Class<?> clazz) {
        List<Field> fields = new ArrayList<>();
        Class<?> current = clazz;

        while (current != null && current != Object.class) {
            fields.addAll(Arrays.asList(current.getDeclaredFields()));
            current = current.getSuperclass();
        }

        return fields;
    }

    /**
     * Détecte automatiquement le type d'un champ Java
     *
     * @param javaType Type Java du champ
     * @return FieldType correspondant, ou null si le type n'est pas supporté
     */
    private static FieldType detectFieldType(Class<?> javaType) {
        // Types numériques
        if (javaType == Integer.class || javaType == int.class ||
                javaType == Long.class || javaType == long.class ||
                javaType == Double.class || javaType == double.class ||
                javaType == Float.class || javaType == float.class ||
                javaType == Short.class || javaType == short.class ||
                javaType == Byte.class || javaType == byte.class ||
                javaType == BigDecimal.class) {
            return FieldType.NUMBER;
        }

        // Types date/temps
        if (javaType == LocalDateTime.class ||
                javaType == LocalDate.class ||
                javaType == java.util.Date.class ||
                javaType == java.sql.Date.class ||
                javaType == java.sql.Timestamp.class) {
            return FieldType.DATE;
        }

        // Type booléen
        if (javaType == Boolean.class || javaType == boolean.class) {
            return FieldType.BOOLEAN;
        }

        // Type String
        if (javaType == String.class) {
            return FieldType.STRING;
        }

        // Autres types non supportés pour l'instant
        return null;
    }

    /**
     * Détermine les opérateurs disponibles selon le type de champ
     */
    private static Set<FilterOperator> getOperatorsForFieldType(FieldType fieldType) {
        return switch (fieldType) {
            case STRING -> Set.of(
                    FilterOperator.EQUALS,
                    FilterOperator.NOT_EQUALS,
                    FilterOperator.CONTAINS,
                    FilterOperator.NOT_CONTAINS,
                    FilterOperator.STARTS_WITH,
                    FilterOperator.ENDS_WITH,
                    FilterOperator.IN,
                    FilterOperator.NOT_IN
            );
            case NUMBER, DATE, BOOLEAN -> Set.of(
                    FilterOperator.EQUALS,
                    FilterOperator.NOT_EQUALS,
                    FilterOperator.GREATER_THAN,
                    FilterOperator.LESS_THAN,
                    FilterOperator.IN,
                    FilterOperator.NOT_IN,
                    FilterOperator.BETWEEN
            );
        };
    }

}
