package io.github.cnadjim.dynamic.search.example.mapper;

import io.github.cnadjim.dynamic.search.example.entity.elastic.OperatingSystemElasticDocument;
import io.github.cnadjim.dynamic.search.example.model.OperatingSystemModel;

/**
 * Mapper Model ↔ Elasticsearch Document
 * Conversion entre le modèle domaine et le document Elasticsearch
 */
public final class OperatingSystemElasticMapper {

    private OperatingSystemElasticMapper() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Convertit un modèle domaine vers un document Elasticsearch
     */
    public static OperatingSystemElasticDocument toDocument(OperatingSystemModel model) {
        if (model == null) {
            return null;
        }

        return OperatingSystemElasticDocument.builder()
                .name(model.getName())
                .version(model.getVersion())
                .kernel(model.getKernel())
                .releaseDate(model.getReleaseDate())
                .usages(model.getUsages())
                .build();
    }

    /**
     * Convertit un document Elasticsearch vers un modèle domaine
     */
    public static OperatingSystemModel toModel(OperatingSystemElasticDocument document) {
        if (document == null) {
            return null;
        }

        return OperatingSystemModel.builder()
                .name(document.getName())
                .version(document.getVersion())
                .kernel(document.getKernel())
                .releaseDate(document.getReleaseDate())
                .usages(document.getUsages())
                .build();
    }

}
