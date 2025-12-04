package io.github.cnadjim.dynamic.search.annotation;

import java.lang.annotation.*;

/**
 * Annotation pour marquer une classe Domain/Entity comme "searchable"
 * Active automatiquement les fonctionnalités de recherche dynamique pour cette entité
 * Usage:
 * <pre>
 * {@code @EnableSearchable}
 * {@code @Entity} // ou @Document pour MongoDB
 * public class OperatingSystem {
 *     // ...
 * }
 * </pre>
 *
 * Cette annotation déclenche l'enregistrement automatique des beans:
 * - SearchUseCase<T>
 * - GetAvailableFiltersUseCase<T>
 */
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface EnableSearchable {

    /**
     * Nom personnalisé pour le bean (optionnel)
     * Par défaut: searchUseCase<NomClasse>
     */
    String beanName() default "";

}
