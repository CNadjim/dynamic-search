package io.github.cnadjim.dynamic.search.annotation;

import java.lang.annotation.*;

/**
 * Annotation pour exclure un champ de la recherche dynamique
 *
 * Par défaut, tous les champs d'une classe annotée @EnableSearchable sont searchable.
 * Utilisez @SearchableExclude pour masquer certains champs sensibles ou non pertinents.
 *
 * Usage:
 * <pre>
 * {@code @EnableSearchable}
 * {@code @Entity}
 * public class User {
 *     {@code @Searchable}(type = FieldType.STRING)  // Explicite (optionnel si type auto-détectable)
 *     private String username;
 *
 *     private String email;  // Auto-détecté comme searchable
 *
 *     {@code @SearchableExclude}  // Exclu de la recherche
 *     private String password;
 *
 *     {@code @SearchableExclude}
 *     private String resetToken;
 * }
 * </pre>
 */
@Target(ElementType.FIELD)
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface SearchableExclude {
    // Annotation marker - pas de paramètres nécessaires
}
