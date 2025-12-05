package io.github.cnadjim.dynamic.search.example.mapper;

import io.github.cnadjim.dynamic.search.example.entity.jpa.OperatingSystemJpaEntity;
import io.github.cnadjim.dynamic.search.example.model.OperatingSystemModel;

/**
 * Mapper Model ↔ JPA Entity
 * Conversion entre le modèle domaine et l'entité JPA PostgreSQL
 */
public final class OperatingSystemJpaMapper {

    private OperatingSystemJpaMapper() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Convertit un modèle domaine vers une entité JPA
     */
    public static OperatingSystemJpaEntity toEntity(OperatingSystemModel model) {
        if (model == null) {
            return null;
        }

        return OperatingSystemJpaEntity.builder()
                .name(model.getName())
                .version(model.getVersion())
                .kernel(model.getKernel())
                .releaseDate(model.getReleaseDate())
                .usages(model.getUsages())
                .build();
    }

    /**
     * Convertit une entité JPA vers un modèle domaine
     */
    public static OperatingSystemModel toModel(OperatingSystemJpaEntity entity) {
        if (entity == null) {
            return null;
        }

        return OperatingSystemModel.builder()
                .name(entity.getName())
                .version(entity.getVersion())
                .kernel(entity.getKernel())
                .releaseDate(entity.getReleaseDate())
                .usages(entity.getUsages())
                .build();
    }

}
