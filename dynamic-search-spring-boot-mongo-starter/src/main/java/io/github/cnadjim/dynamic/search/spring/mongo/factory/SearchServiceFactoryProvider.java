package io.github.cnadjim.dynamic.search.spring.mongo.factory;

import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;
import io.github.cnadjim.dynamic.search.port.out.EntityRepository;
import io.github.cnadjim.dynamic.search.service.SearchService;
import io.github.cnadjim.dynamic.search.spring.mongo.adapter.MongoEntityRepositoryAdapter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.MongoPersistentEntity;

/**
 * Factory provider pour créer des instances de SearchService<T> avec injection Spring.
 *
 * Ce provider permet de créer dynamiquement des SearchService pour les documents MongoDB
 * annotés @EnableSearchable.
 *
 * Note: Enregistré automatiquement par SearchableMongoBeanRegistrar.
 */
@Slf4j
@RequiredArgsConstructor
public class SearchServiceFactoryProvider {

    private final MongoTemplate mongoTemplate;
    private final EntityMetadataStorage metadataStorage;

    /**
     * Crée un SearchService pour un document MongoDB donné
     */
    public <T> SearchService<T> createSearchService(Class<T> documentClass) {
        log.debug("Creating SearchService for document: {}", documentClass.getSimpleName());

        try {
            // Obtenir le MongoPersistentEntity depuis le MongoTemplate
            MongoPersistentEntity<?> persistentEntity = mongoTemplate.getConverter()
                    .getMappingContext()
                    .getPersistentEntity(documentClass);

            if (persistentEntity == null) {
                throw new IllegalArgumentException("No persistent entity found for document class: " + documentClass.getName());
            }

            // Créer l'adaptateur
            EntityRepository<T> repositoryAdapter = new MongoEntityRepositoryAdapter<>(mongoTemplate, documentClass, persistentEntity);

            // Créer le service avec metadataStorage
            return new SearchService<>(repositoryAdapter, metadataStorage, documentClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SearchService for document: " + documentClass.getName(), e);
        }
    }
}
