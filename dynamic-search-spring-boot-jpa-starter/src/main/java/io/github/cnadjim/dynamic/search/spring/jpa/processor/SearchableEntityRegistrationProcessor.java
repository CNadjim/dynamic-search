package io.github.cnadjim.dynamic.search.spring.jpa.processor;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.model.EntityRepository;
import io.github.cnadjim.dynamic.search.port.in.RegisterEntityUseCase;
import io.github.cnadjim.dynamic.search.spring.jpa.adapter.JpaEntityRepositoryAdapter;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.jpa.repository.support.JpaEntityInformation;
import org.springframework.data.jpa.repository.support.JpaEntityInformationSupport;
import org.springframework.data.jpa.repository.support.SimpleJpaRepository;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processor responsable de scanner les entités JPA annotées @EnableSearchable
 * et de les enregistrer auprès du SearchService unique.
 * <p>
 * Utilise ApplicationListener<ContextRefreshedEvent> pour enregistrer les entités
 * après que tous les beans soient créés et disponibles.
 * <p>
 * L'enregistrement se fait une seule fois lors du premier ContextRefreshedEvent.
 */
@Slf4j
public class SearchableEntityRegistrationProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final RegisterEntityUseCase registerEntityUseCase;
    private final EntityManager entityManager;
    private final AtomicBoolean registered = new AtomicBoolean(false);

    public SearchableEntityRegistrationProcessor(RegisterEntityUseCase registerEntityUseCase, EntityManager entityManager) {
        this.registerEntityUseCase = registerEntityUseCase;
        this.entityManager = entityManager;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ne s'exécuter qu'une seule fois
        if (!registered.compareAndSet(false, true)) {
            return;
        }

        log.info("🔍 Scanning for @EnableSearchable JPA entities...");

        // Scanner le classpath complet pour trouver les classes annotées @Entity et @EnableSearchable
        ClassPathScanningCandidateComponentProvider scanner = createScanner();

        // Scanner en partant de la racine (tous les packages)
        Set<BeanDefinition> candidates = scanner.findCandidateComponents("");

        int registeredCount = 0;
        for (BeanDefinition candidate : candidates) {
            try {
                Class<?> entityClass = Class.forName(candidate.getBeanClassName());

                // Vérifier que la classe a bien les deux annotations
                if (entityClass.isAnnotationPresent(Entity.class) &&
                        entityClass.isAnnotationPresent(EnableSearchable.class)) {

                    log.info("✅ Registering @EnableSearchable JPA entity: {}", entityClass.getSimpleName());
                    registerEntity(entityClass);
                    registeredCount++;
                }
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load class: {}", candidate.getBeanClassName(), e);
            } catch (Exception e) {
                log.error("Failed to register entity: {}", candidate.getBeanClassName(), e);
            }
        }

        log.info("📊 Successfully registered {} @EnableSearchable JPA entities", registeredCount);
    }

    /**
     * Crée un scanner configuré pour trouver les classes annotées @Entity et @EnableSearchable
     */
    private ClassPathScanningCandidateComponentProvider createScanner() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        // Scanner les classes avec @Entity ET @EnableSearchable
        scanner.addIncludeFilter(new AnnotationTypeFilter(Entity.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(EnableSearchable.class));

        return scanner;
    }

    /**
     * Enregistre une entité JPA auprès du SearchService
     * Crée un adaptateur JPA spécifique pour cette entité
     */
    private <T> void registerEntity(Class<T> entityClass) {
        // Créer le JpaEntityInformation
        JpaEntityInformation<T, ?> entityInformation = JpaEntityInformationSupport.getEntityInformation(entityClass, entityManager);

        // Créer le SimpleJpaRepository
        SimpleJpaRepository<T, ?> jpaRepository = new SimpleJpaRepository<>(entityInformation, entityManager);

        // Créer l'adaptateur
        EntityRepository<T> repositoryAdapter = new JpaEntityRepositoryAdapter<>(jpaRepository);

        // Enregistrer l'entité auprès du SearchService
        registerEntityUseCase.registerEntity(entityClass, repositoryAdapter);
    }
}
