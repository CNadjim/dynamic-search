package io.github.cnadjim.dynamic.search.example.mapper;

import io.github.cnadjim.dynamic.search.example.entity.mongo.OperatingSystemMongoDocument;
import io.github.cnadjim.dynamic.search.example.model.OperatingSystemModel;

/**
 * Mapper Model ↔ MongoDB Document
 * Conversion entre le modèle domaine et le document MongoDB
 */
public final class OperatingSystemMongoMapper {

    private OperatingSystemMongoMapper() {
        // Classe utilitaire - constructeur privé
    }

    /**
     * Convertit un modèle domaine vers un document MongoDB
     */
    public static OperatingSystemMongoDocument toDocument(OperatingSystemModel model) {
        if (model == null) {
            return null;
        }

        return OperatingSystemMongoDocument.builder()
                .name(model.getName())
                .version(model.getVersion())
                .kernel(model.getKernel())
                .releaseDate(model.getReleaseDate())
                .usages(model.getUsages())
                .build();
    }

    /**
     * Convertit un document MongoDB vers un modèle domaine
     */
    public static OperatingSystemModel toModel(OperatingSystemMongoDocument document) {
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
