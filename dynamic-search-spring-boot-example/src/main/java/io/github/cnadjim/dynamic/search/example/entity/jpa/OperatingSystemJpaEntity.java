package io.github.cnadjim.dynamic.search.example.entity.jpa;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.annotation.SearchableExclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.persistence.*;
import java.io.Serializable;
import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * Entité JPA (PostgreSQL) - Représentation technique pour la persistance relationnelle
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
@EnableSearchable(beanName = "operatingSystemJpaSearchUseCase")
public class OperatingSystemJpaEntity implements Serializable {

    @SearchableExclude
    private static final long serialVersionUID = -1730538653948604611L;

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "name", nullable = false)
    private String name;

    @Column(name = "version", nullable = false)
    private String version;

    @Column(name = "kernel", nullable = false)
    private String kernel;

    @Column(name = "release_date", nullable = false)
    private LocalDate releaseDate;

    @Column(name = "usages", nullable = false)
    private Integer usages;

}
