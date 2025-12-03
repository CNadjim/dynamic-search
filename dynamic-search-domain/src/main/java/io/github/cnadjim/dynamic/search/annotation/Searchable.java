package io.github.cnadjim.dynamic.search.annotation;

import io.github.cnadjim.dynamic.search.model.FieldType;

import java.lang.annotation.*;

/**
 * Annotation pour marquer un champ comme filtrable dans les recherches dynamiques
 * Permet de définir le type de champ et si le champ est nullable
 *
 * Usage:
 * <pre>
 * {@code @Searchable}(type = FieldType.STRING)
 * private String name;
 *
 * {@code @Searchable}(type = FieldType.NUMBER, nullable = true)
 * private Integer age;
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface Searchable {

    /**
     * Type du champ pour les opérations de filtrage
     */
    FieldType type();

    /**
     * Indique si le champ peut être null
     * Par défaut: true
     */
    boolean nullable() default true;

    /**
     * Nom du champ pour les requêtes (optionnel)
     * Par défaut: nom du champ Java
     */
    String fieldName() default "";

}
