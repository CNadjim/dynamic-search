package io.github.cnadjim.dynamic.search.spring.elasticsearch.factory;

import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;
import io.github.cnadjim.dynamic.search.port.out.EntityRepository;
import io.github.cnadjim.dynamic.search.service.SearchService;
import io.github.cnadjim.dynamic.search.spring.elasticsearch.adapter.ElasticsearchEntityRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

/**
 * Factory provider pour créer des instances de SearchService<T> avec injection Spring.
 *
 * Ce provider permet de créer dynamiquement des SearchService pour les documents Elasticsearch
 * annotés @EnableSearchable.
 *
 * Note: Enregistré automatiquement par SearchableElasticsearchBeanRegistrar.
 */
@Slf4j
@RequiredArgsConstructor
public class SearchServiceFactoryProvider {

    private final ElasticsearchOperations elasticsearchOperations;
    private final EntityMetadataStorage metadataStorage;

    /**
     * Crée un SearchService pour un document Elasticsearch donné
     */
    public <T> SearchService<T> createSearchService(Class<T> documentClass) {
        log.debug("Creating SearchService for document: {}", documentClass.getSimpleName());

        try {
            // Créer l'adaptateur
            EntityRepository<T> repositoryAdapter = new ElasticsearchEntityRepositoryAdapter<>(elasticsearchOperations, documentClass);

            // Créer le service avec metadataStorage
            return new SearchService<>(repositoryAdapter, metadataStorage, documentClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SearchService for document: " + documentClass.getName(), e);
        }
    }
}
