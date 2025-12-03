package io.github.cnadjim.dynamic.search.spring.starter.response;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import io.github.cnadjim.dynamic.search.model.FilterDescriptor;
import io.swagger.v3.oas.annotations.media.Schema;

import java.io.Serializable;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * DTO REST - Descripteur de filtre disponible pour une entité
 * Utilisé pour exposer les métadonnées de filtrage via l'API REST
 * Record Java immuable cohérent avec les enums de réponse
 */
@JsonIgnoreProperties(ignoreUnknown = true)
@Schema(description = "Descripteur d'un champ filtrable avec ses opérateurs disponibles")
public record FilterDescriptorResponse(

        @Schema(
                description = "Nom du champ filtrable",
                example = "name",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        String key,

        @Schema(
                description = "Type du champ",
                example = "string",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        FieldTypeResponse fieldType,

        @Schema(
                description = "Indique si le champ peut être null",
                example = "false",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        boolean nullable,

        @Schema(
                description = "Liste des opérateurs de filtrage disponibles pour ce champ",
                example = "[\"equals\", \"notEquals\", \"contains\", \"startsWith\", \"endsWith\"]",
                requiredMode = Schema.RequiredMode.REQUIRED
        )
        Set<FilterOperatorResponse> availableOperators

) implements Serializable {

    /**
     * Constructeur compact avec validations
     */
    public FilterDescriptorResponse {
        if (key == null || key.isBlank()) {
            throw new IllegalArgumentException("Filter key cannot be null or blank");
        }
        if (fieldType == null) {
            throw new IllegalArgumentException("Field type cannot be null");
        }
        if (availableOperators == null || availableOperators.isEmpty()) {
            throw new IllegalArgumentException("Available operators cannot be null or empty");
        }
    }

    /**
     * Convertit un FilterDescriptor du domaine vers le DTO REST
     */
    public static FilterDescriptorResponse fromDomain(FilterDescriptor descriptor) {
        if (descriptor == null) {
            throw new IllegalArgumentException("FilterDescriptor cannot be null");
        }

        return new FilterDescriptorResponse(
                descriptor.key(),
                FieldTypeResponse.fromDomain(descriptor.fieldType()),
                descriptor.nullable(),
                descriptor.availableOperators().stream()
                        .map(FilterOperatorResponse::fromDomain)
                        .collect(Collectors.toSet())
        );
    }

    /**
     * Convertit le DTO REST vers le modèle du domaine
     */
    public FilterDescriptor toDomain() {
        return new FilterDescriptor(
                this.key,
                this.fieldType.toDomain(),
                this.nullable,
                this.availableOperators.stream()
                        .map(FilterOperatorResponse::toDomain)
                        .collect(Collectors.toSet())
        );
    }
}
