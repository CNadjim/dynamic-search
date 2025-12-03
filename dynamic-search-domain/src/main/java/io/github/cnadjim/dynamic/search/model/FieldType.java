package io.github.cnadjim.dynamic.search.model;

/**
 * Types de champs supportés dans le domaine
 */
public enum FieldType {
    /**
     * Type chaîne de caractères - Pour tous les champs texte
     */
    STRING,

    /**
     * Type numérique - Pour Integer, Long, Double, Float, BigDecimal, etc.
     */
    NUMBER,

    /**
     * Type date/heure - Pour LocalDateTime, LocalDate, Date, etc.
     */
    DATE,

    /**
     * Type booléen - Pour les valeurs true/false
     */
    BOOLEAN
}
