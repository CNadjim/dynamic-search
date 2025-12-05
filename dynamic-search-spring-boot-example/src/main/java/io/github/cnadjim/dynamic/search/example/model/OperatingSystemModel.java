package io.github.cnadjim.dynamic.search.example.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * Modèle domaine - Représentation métier d'un Operating System
 * Indépendant des technologies de persistance (JPA, MongoDB, Elasticsearch)
 * Sert de modèle canonique pour la génération et les transformations
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OperatingSystemModel {

    private String name;
    private String version;
    private String kernel;
    private LocalDate releaseDate;
    private Integer usages;

}
