package io.github.cnadjim.dynamic.search.example.controller;

import io.github.cnadjim.dynamic.search.model.SearchResult;
import io.github.cnadjim.dynamic.search.example.entity.OperatingSystemEntity;
import io.github.cnadjim.dynamic.search.spring.starter.gateway.SearchGateway;
import io.github.cnadjim.dynamic.search.spring.starter.request.SearchRequest;
import io.github.cnadjim.dynamic.search.spring.starter.response.FilterDescriptorResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * Contrôleur REST - Exemple d'utilisation du starter dynamic-search
 * Expose l'API REST pour la recherche d'Operating Systems
 * Utilise le SearchGateway comme façade unifiée
 */
@Slf4j
@RestController
@RequestMapping(value = "/api/operating-systems")
@RequiredArgsConstructor
public class OperatingSystemController {

    // SearchGateway auto-configuré par le starter ! 🎉
    private final SearchGateway searchGateway;

    /**
     * Endpoint pour récupérer les filtres disponibles
     * Permet au client de découvrir dynamiquement les champs filtrables
     * Retourne les descripteurs au format REST avec enums cohérents
     */
    @GetMapping(value = "/filters", produces = MediaType.APPLICATION_JSON_VALUE)
    public List<FilterDescriptorResponse> getAvailableFilters() {
        log.info("Fetching available filters for OperatingSystemEntity");
        return searchGateway.getAvailableFilters(OperatingSystemEntity.class);
    }

    /**
     * Endpoint de recherche dynamique
     * Le SearchGateway gère automatiquement la conversion et le dispatch
     */
    @PostMapping(value = "/search", consumes = MediaType.APPLICATION_JSON_VALUE, produces = MediaType.APPLICATION_JSON_VALUE)
    public SearchResult<OperatingSystemEntity> search(@Valid @RequestBody SearchRequest request) {
        log.info("Received search request with {} filters and {} sorts", request.getFilters().size(), request.getSorts().size());
        return searchGateway.search(request, OperatingSystemEntity.class);
    }

}
