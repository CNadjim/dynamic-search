package io.github.cnadjim.dynamic.search.spring.jpa.factory;

import io.github.cnadjim.dynamic.search.port.out.EntityMetadataStorage;
import io.github.cnadjim.dynamic.search.port.out.EntityRepository;
import io.github.cnadjim.dynamic.search.service.SearchService;
import io.github.cnadjim.dynamic.search.spring.jpa.adapter.JpaEntityRepositoryAdapter;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

/**
 * Factory provider pour créer des instances de SearchService<T> avec injection Spring.
 *
 * Ce provider permet de créer dynamiquement des SearchService pour les entités JPA
 * annotées @EnableSearchable.
 *
 * Note: Enregistré automatiquement par SearchableJpaAutoConfiguration.
 */
@Slf4j
@RequiredArgsConstructor
public class SearchServiceFactoryProvider {

    private final ObjectProvider<EntityManager> entityManagerProvider;
    private final EntityMetadataStorage metadataStorage;

    /**
     * Crée un SearchService pour une entité JPA donnée
     */
    @SuppressWarnings({"unchecked", "rawtypes"})
    public <T> SearchService<T> createSearchService(Class<T> entityClass) {
        log.debug("Creating SearchService for entity: {}", entityClass.getSimpleName());

        try {
            // Obtenir l'EntityManager via le provider
            EntityManager entityManager = entityManagerProvider.getObject();

            // Créer le JpaEntityInformation
            JpaEntityInformation entityInformation = JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);

            // Créer le SimpleJpaRepository
            SimpleJpaRepository repository = new SimpleJpaRepository(entityInformation, entityManager);

            // Créer l'adaptateur
            EntityRepository<T> repositoryAdapter = new JpaEntityRepositoryAdapter<>(repository, entityManager, entityClass);

            // Créer le service avec metadataStorage
            return new SearchService<>(repositoryAdapter, metadataStorage, entityClass);
        } catch (Exception e) {
            throw new RuntimeException("Failed to create SearchService for entity: " + entityClass.getName(), e);
        }
    }
}
