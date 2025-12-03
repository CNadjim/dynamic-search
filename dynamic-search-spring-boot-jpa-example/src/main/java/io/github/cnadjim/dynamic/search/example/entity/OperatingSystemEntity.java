package io.github.cnadjim.dynamic.search.example.entity;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.annotation.SearchableExclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * Entité JPA - Représentation technique pour la persistance
 *
 * @EnableSearchable active automatiquement la recherche dynamique pour cette entité
 *
 * Par défaut, TOUS les champs sont searchable (auto-détection du type)
 * Utilisez @SearchableExclude pour exclure certains champs
 * Utilisez @Searchable pour personnaliser les métadonnées (fieldName, nullable, etc.)
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "operating_system")
@EnableSearchable
public class OperatingSystemEntity implements Serializable {

    @SearchableExclude // Exclure serialVersionUID de la recherche
    private static final long serialVersionUID = -1730538653948604611L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;  // Auto-détecté comme NUMBER

    @Column(name = "name", nullable = false)
    private String name;  // Auto-détecté comme STRING

    @Column(name = "version", nullable = false)
    private String version;  // Auto-détecté comme STRING

    @Column(name = "kernel", nullable = false)
    private String kernel;  // Auto-détecté comme STRING

    @Column(name = "release_date", nullable = false)
    private LocalDateTime releaseDate;  // Auto-détecté comme DATE

    @Column(name = "usages", nullable = false)
    private Integer usages;  // Auto-détecté comme NUMBER

}
