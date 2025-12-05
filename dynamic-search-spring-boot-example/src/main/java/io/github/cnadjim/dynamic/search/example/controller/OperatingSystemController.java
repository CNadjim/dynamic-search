package io.github.cnadjim.dynamic.search.example.controller;

import io.github.cnadjim.dynamic.search.example.entity.elastic.OperatingSystemElasticDocument;
import io.github.cnadjim.dynamic.search.example.entity.jpa.OperatingSystemJpaEntity;
import io.github.cnadjim.dynamic.search.example.entity.mongo.OperatingSystemMongoDocument;
import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.spring.starter.gateway.SearchGateway;
import io.github.cnadjim.dynamic.search.spring.starter.request.SearchRequest;
import io.github.cnadjim.dynamic.search.spring.starter.response.FilterDescriptorResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST - Exemple d'utilisation multi-technologie du starter dynamic-search
 * Expose l'API REST pour la recherche d'Operating Systems sur JPA, MongoDB et Elasticsearch
 * Utilise le SearchGateway comme façade unifiée
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/operating-systems")
@RequiredArgsConstructor
@CrossOrigin(origins = {"http://localhost:5173", "http://localhost:8080"})
@Tag(name = "Operating Systems", description = "API de recherche multi-technologie d'Operating Systems")
public class OperatingSystemController {

    private final SearchGateway searchGateway;

    // ==================== JPA (PostgreSQL) ====================

    @Operation(summary = "Récupère les filtres disponibles pour JPA")
    @GetMapping(value = "/jpa/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FilterDescriptorResponse> getJpaFilters() {
        log.info("Fetching JPA filters for OperatingSystemJpaEntity");
        return searchGateway.getAvailableFilters(OperatingSystemJpaEntity.class);
    }

    @Operation(summary = "Recherche dynamique sur PostgreSQL via JPA")
    @PostMapping(value = "/jpa/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchResult<OperatingSystemJpaEntity> searchJpa(@Valid @RequestBody SearchRequest request) {
        log.info("JPA search: {} filters, {} sorts, fullText: {}",
                request.getFilters().size(),
                request.getSorts().size(),
                request.getFullText() != null ? request.getFullText().query() : "none");
        return searchGateway.search(request, OperatingSystemJpaEntity.class);
    }

    // ==================== MongoDB ====================

    @Operation(summary = "Récupère les filtres disponibles pour MongoDB")
    @GetMapping(value = "/mongo/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FilterDescriptorResponse> getMongoFilters() {
        log.info("Fetching MongoDB filters for OperatingSystemMongoDocument");
        return searchGateway.getAvailableFilters(OperatingSystemMongoDocument.class);
    }

    @Operation(summary = "Recherche dynamique sur MongoDB")
    @PostMapping(value = "/mongo/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchResult<OperatingSystemMongoDocument> searchMongo(@Valid @RequestBody SearchRequest request) {
        log.info("MongoDB search: {} filters, {} sorts, fullText: {}",
                request.getFilters().size(),
                request.getSorts().size(),
                request.getFullText() != null ? request.getFullText().query() : "none");
        return searchGateway.search(request, OperatingSystemMongoDocument.class);
    }

    // ==================== Elasticsearch ====================

    @Operation(summary = "Récupère les filtres disponibles pour Elasticsearch")
    @GetMapping(value = "/elastic/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FilterDescriptorResponse> getElasticFilters() {
        log.info("Fetching Elasticsearch filters for OperatingSystemElasticDocument");
        return searchGateway.getAvailableFilters(OperatingSystemElasticDocument.class);
    }

    @Operation(summary = "Recherche dynamique sur Elasticsearch")
    @PostMapping(value = "/elastic/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchResult<OperatingSystemElasticDocument> searchElastic(@Valid @RequestBody SearchRequest request) {
        log.info("Elasticsearch search: {} filters, {} sorts, fullText: {}",
                request.getFilters().size(),
                request.getSorts().size(),
                request.getFullText() != null ? request.getFullText().query() : "none");
        return searchGateway.search(request, OperatingSystemElasticDocument.class);
    }

}
