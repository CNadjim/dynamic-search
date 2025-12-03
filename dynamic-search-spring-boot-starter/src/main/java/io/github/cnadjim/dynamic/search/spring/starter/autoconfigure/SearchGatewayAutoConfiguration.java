package io.github.cnadjim.dynamic.search.spring.starter.autoconfigure;

import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;
import io.github.cnadjim.dynamic.search.port.stub.InMemoryEntityMetadataStorage;
import io.github.cnadjim.dynamic.search.spring.starter.gateway.DefaultSearchGateway;
import io.github.cnadjim.dynamic.search.spring.starter.gateway.SearchGateway;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Bean;

/**
 * Auto-configuration pour le SearchGateway
 *
 * Crée automatiquement un bean SearchGateway qui fait office de façade
 * pour les opérations de recherche dynamique
 *
 * Cette configuration :
 * - Crée un bean SearchGateway par défaut
 * - Permet aux utilisateurs de fournir leur propre implémentation via @ConditionalOnMissingBean
 * - Injecte automatiquement l'ApplicationContext pour la résolution dynamique des use cases
 * - Injecte EntityMetadataStorage pour la déduction automatique des fieldType
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

    @Bean
    @ConditionalOnMissingBean
    public EntityMetadataStorage entityMetadataStorage(){
        return new InMemoryEntityMetadataStorage();
    }

    /**
     * Crée le bean SearchGateway par défaut
     * Utilise l'implémentation DefaultSearchGateway qui résout les use cases dynamiquement
     *
     * @param applicationContext Le contexte Spring pour la résolution des beans
     * @param metadataStorage Le storage des métadonnées pour la déduction des fieldType
     * @return SearchGateway configuré
     */
    @Bean
    @ConditionalOnMissingBean
    public SearchGateway searchGateway(ApplicationContext applicationContext, EntityMetadataStorage metadataStorage) {
        log.debug("Creating default SearchGateway bean with automatic fieldType resolution");
        return new DefaultSearchGateway(applicationContext, metadataStorage);
    }
}
