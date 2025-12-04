package io.github.cnadjim.dynamic.search.spring.starter.util;

import io.github.cnadjim.dynamic.search.model.FieldType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Parseur de types de champs - Convertit les valeurs String en types appropriés
 * Utilitaire partagé entre les différentes implémentations (JPA, MongoDB)
 */
public final class FieldTypeParser {

    private static final Logger log = Logger.getLogger(FieldTypeParser.class.getName());

    // Liste des formats de date supportés (du plus spécifique au plus général)
    private static final List<DateTimeFormatter> DATE_FORMATTERS = List.of(
            DateTimeFormatter.ISO_LOCAL_DATE_TIME,              // 2024-12-03T10:00:00
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"), // 2024-12-03 10:00:00
            DateTimeFormatter.ofPattern("dd-MM-yyyy HH:mm:ss"), // 03-12-2024 10:00:00
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss"), // 03/12/2024 10:00:00
            DateTimeFormatter.ISO_LOCAL_DATE                    // 2024-12-03 (sera converti en LocalDateTime à minuit)
    );

    private FieldTypeParser() {
        // Classe utilitaire - constructeur privé
    }

    public static Object parse(FieldType fieldType, String value) {
        if (value == null) {
            return null;
        }

        try {
            return switch (fieldType) {
                case BOOLEAN -> Boolean.valueOf(value);
                case DATE -> parseDateTime(value);
                case NUMBER -> parseNumber(value);
                case STRING -> value;
            };
        } catch (Exception e) {
            log.log(Level.SEVERE, "Failed to parse field type " + fieldType + " with value " + value + ": " + e.getMessage());
            return value;
        }
    }

    /**
     * Parse une date en essayant plusieurs formats courants
     * Supporte ISO 8601, formats européens et américains
     */
    private static LocalDateTime parseDateTime(String value) {
        // Essayer chaque format dans l'ordre
        for (DateTimeFormatter formatter : DATE_FORMATTERS) {
            try {
                // Pour ISO_LOCAL_DATE, on obtient un LocalDate qu'il faut convertir en LocalDateTime
                if (formatter == DateTimeFormatter.ISO_LOCAL_DATE) {
                    LocalDate date = LocalDate.parse(value, formatter);
                    return date.atStartOfDay(); // Minuit du jour
                }
                return LocalDateTime.parse(value, formatter);
            } catch (DateTimeParseException e) {
                // Continuer avec le prochain format
            }
        }

        // Si aucun format ne fonctionne, logger et lancer une exception
        log.log(Level.SEVERE, "Failed to parse date value: " + value + ". Supported formats: ISO (yyyy-MM-dd, yyyy-MM-ddTHH:mm:ss), " +
                "European (dd-MM-yyyy HH:mm:ss, dd/MM/yyyy HH:mm:ss)");
        throw new IllegalArgumentException("Cannot parse date: " + value);
    }

    /**
     * Parse un nombre de façon intelligente
     * Essaie Integer, puis Long, puis Double, puis BigDecimal
     */
    private static Number parseNumber(String value) {
        try {
            // Si pas de point décimal, essayer Integer puis Long
            if (!value.contains(".")) {
                try {
                    return Integer.parseInt(value);
                } catch (NumberFormatException e) {
                    return Long.parseLong(value);
                }
            }
            // Sinon essayer Double puis BigDecimal
            try {
                return Double.parseDouble(value);
            } catch (NumberFormatException e) {
                return new BigDecimal(value);
            }
        } catch (Exception e) {
            log.log(Level.WARNING, "Failed to parse number value: " + value);
            return 0;
        }
    }

}
