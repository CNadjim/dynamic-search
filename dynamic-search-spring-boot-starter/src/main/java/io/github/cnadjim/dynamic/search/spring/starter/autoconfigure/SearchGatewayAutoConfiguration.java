package io.github.cnadjim.dynamic.search.spring.starter.autoconfigure;

import io.github.cnadjim.dynamic.search.port.in.GetAvailableFiltersUseCase;
import io.github.cnadjim.dynamic.search.port.in.GetFieldTypeUseCase;
import io.github.cnadjim.dynamic.search.port.in.SearchUseCase;
import io.github.cnadjim.dynamic.search.port.out.EntityDescriptorStorage;
import io.github.cnadjim.dynamic.search.port.stub.InMemoryEntityDescriptorStorage;
import io.github.cnadjim.dynamic.search.service.SearchService;
import io.github.cnadjim.dynamic.search.spring.starter.gateway.DefaultSearchGateway;
import io.github.cnadjim.dynamic.search.spring.starter.gateway.SearchGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration pour le SearchGateway et SearchService
 *
 * Crée automatiquement les beans nécessaires pour la recherche dynamique :
 * - EntityDescriptorStorage : Stockage en mémoire des métadonnées des entités
 * - SearchService : Service unique qui gère toutes les entités enregistrées
 * - SearchGateway : Façade REST pour les opérations de recherche
 *
 * Cette configuration :
 * - Crée un bean SearchService unique partagé par toutes les entités
 * - Crée un bean SearchGateway par défaut
 * - Permet aux utilisateurs de fournir leur propre implémentation via @ConditionalOnMissingBean
 * - Injecte automatiquement l'ApplicationContext pour la résolution dynamique des use cases
 *
 * Usage dans un contrôleur :
 * <pre>
 * {@code
 * @RestController
 * @RequiredArgsConstructor
 * public class MyController {
 *     private final SearchGateway searchGateway;
 *
 *     @PostMapping("/search")
 *     public SearchResult<MyEntity> search(@RequestBody SearchRequest request) {
 *         return searchGateway.search(request, MyEntity.class);
 *     }
 * }
 * }
 * </pre>
 */
@Slf4j
@AutoConfiguration
public class SearchGatewayAutoConfiguration {

    public SearchGatewayAutoConfiguration() {
        log.info("✅ SearchGatewayAutoConfiguration activated - SearchGateway will be available");
    }

    /**
     * Crée le bean EntityDescriptorStorage
     * Stockage en mémoire des métadonnées des entités
     */
    @Bean
    @ConditionalOnMissingBean
    public EntityDescriptorStorage entityDescriptorStorage(){
        log.debug("Creating EntityDescriptorStorage bean");
        return new InMemoryEntityDescriptorStorage();
    }

    /**
     * Crée le bean SearchService unique
     * Ce service gère toutes les entités enregistrées via registerEntity()
     *
     * @param descriptorStorage Le storage des descripteurs d'entités
     * @return SearchService configuré
     */
    @Bean
    @ConditionalOnMissingBean
    public SearchService searchService(EntityDescriptorStorage descriptorStorage) {
        log.debug("Creating SearchService bean");
        return new SearchService(descriptorStorage);
    }

    /**
     * Crée le bean SearchGateway par défaut
     * Utilise l'implémentation DefaultSearchGateway qui injecte directement les use cases
     * @return SearchGateway configuré
     */
    @Bean
    @ConditionalOnMissingBean
    public SearchGateway searchGateway(SearchUseCase searchUseCase,
                                       GetAvailableFiltersUseCase getAvailableFiltersUseCase,
                                       GetFieldTypeUseCase getFieldTypeUseCase) {
        log.debug("Creating SearchGateway bean");
        return new DefaultSearchGateway(searchUseCase, getAvailableFiltersUseCase, getFieldTypeUseCase);
    }
}
