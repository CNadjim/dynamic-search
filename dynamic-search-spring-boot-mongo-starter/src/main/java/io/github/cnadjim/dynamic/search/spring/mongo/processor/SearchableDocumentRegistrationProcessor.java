package io.github.cnadjim.dynamic.search.spring.mongo.processor;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.port.out.EntityRepository;
import io.github.cnadjim.dynamic.search.port.in.RegisterEntityUseCase;
import io.github.cnadjim.dynamic.search.spring.mongo.adapter.MongoEntityRepositoryAdapter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.ApplicationListener;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.context.event.ContextRefreshedEvent;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;

import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Processor responsable de scanner les documents MongoDB annotés @EnableSearchable
 * et de les enregistrer auprès du SearchService unique.
 * <p>
 * Utilise ApplicationListener<ContextRefreshedEvent> pour enregistrer les documents
 * après que tous les beans soient créés et disponibles.
 * <p>
 * L'enregistrement se fait une seule fois lors du premier ContextRefreshedEvent.
 */
@Slf4j
public class SearchableDocumentRegistrationProcessor implements ApplicationListener<ContextRefreshedEvent> {

    private final RegisterEntityUseCase registerEntityUseCase;
    private final MongoTemplate mongoTemplate;
    private final AtomicBoolean registered = new AtomicBoolean(false);

    public SearchableDocumentRegistrationProcessor(RegisterEntityUseCase registerEntityUseCase, MongoTemplate mongoTemplate) {
        this.registerEntityUseCase = registerEntityUseCase;
        this.mongoTemplate = mongoTemplate;
    }

    @Override
    public void onApplicationEvent(ContextRefreshedEvent event) {
        // Ne s'exécuter qu'une seule fois
        if (!registered.compareAndSet(false, true)) {
            return;
        }

        log.info("🔍 Scanning for @EnableSearchable MongoDB documents...");

        // Scanner le classpath complet pour trouver les classes annotées @Document et @EnableSearchable
        ClassPathScanningCandidateComponentProvider scanner = createScanner();

        // Scanner en partant de la racine (tous les packages)
        Set<BeanDefinition> candidates = scanner.findCandidateComponents("");

        int registeredCount = 0;
        for (BeanDefinition candidate : candidates) {
            try {
                Class<?> documentClass = Class.forName(candidate.getBeanClassName());

                // Vérifier que la classe a bien les deux annotations
                if (documentClass.isAnnotationPresent(Document.class) &&
                        documentClass.isAnnotationPresent(EnableSearchable.class)) {

                    log.info("✅ Registering @EnableSearchable MongoDB document: {}", documentClass.getSimpleName());
                    registerDocument(documentClass);
                    registeredCount++;
                }
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load class: {}", candidate.getBeanClassName(), e);
            } catch (Exception e) {
                log.error("Failed to register document: {}", candidate.getBeanClassName(), e);
            }
        }

        log.info("📊 Successfully registered {} @EnableSearchable MongoDB documents", registeredCount);
    }

    /**
     * Crée un scanner configuré pour trouver les classes annotées @Document et @EnableSearchable
     */
    private ClassPathScanningCandidateComponentProvider createScanner() {
        ClassPathScanningCandidateComponentProvider scanner =
                new ClassPathScanningCandidateComponentProvider(false);

        // Scanner les classes avec @Document ET @EnableSearchable
        scanner.addIncludeFilter(new AnnotationTypeFilter(Document.class));
        scanner.addIncludeFilter(new AnnotationTypeFilter(EnableSearchable.class));

        return scanner;
    }

    /**
     * Enregistre un document MongoDB auprès du SearchService
     * Crée un adaptateur MongoDB spécifique pour ce document
     */
    private <T> void registerDocument(Class<T> documentClass) {
        // Créer l'adaptateur
        EntityRepository<T> repositoryAdapter = new MongoEntityRepositoryAdapter<>(mongoTemplate, documentClass);

        // Enregistrer le document auprès du SearchService
        registerEntityUseCase.registerEntity(documentClass, repositoryAdapter);
    }
}
