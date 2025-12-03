package io.github.cnadjim.dynamic.search.spring.mongo.processor;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.service.SearchService;
import io.github.cnadjim.dynamic.search.spring.mongo.factory.SearchServiceFactoryProvider;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.lang.NonNull;

import java.util.Set;

/**
 * Processor responsable de scanner les documents MongoDB annotés @EnableSearchable
 * et d'enregistrer les beans SearchUseCase<T> et GetAvailableFiltersUseCase<T>.
 *
 * Utilise BeanDefinitionRegistryPostProcessor pour scanner le classpath et enregistrer
 * les beans avant la création des autres beans.
 * Les beans sont enregistrés avec un instanceSupplier lazy qui appelle le factory.
 */
@Slf4j
public class SearchableDocumentRegistrationProcessor implements BeanDefinitionRegistryPostProcessor {

    private final SearchServiceFactoryProvider factoryProvider;

    public SearchableDocumentRegistrationProcessor(SearchServiceFactoryProvider factoryProvider) {
        this.factoryProvider = factoryProvider;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(@NonNull BeanDefinitionRegistry registry) throws BeansException {
        log.info("🔍 Scanning for @EnableSearchable MongoDB documents...");

        // Scanner le classpath complet pour trouver les classes annotées @Document et @EnableSearchable
        ClassPathScanningCandidateComponentProvider scanner = createScanner();

        // Scanner en partant de la racine (tous les packages)
        Set<BeanDefinition> candidates = scanner.findCandidateComponents("");

        for (BeanDefinition candidate : candidates) {
            try {
                Class<?> documentClass = Class.forName(candidate.getBeanClassName());

                // Vérifier que la classe a bien les deux annotations
                if (documentClass.isAnnotationPresent(Document.class) &&
                    documentClass.isAnnotationPresent(EnableSearchable.class)) {

                    EnableSearchable annotation = documentClass.getAnnotation(EnableSearchable.class);

                    String baseName = annotation.beanName().isEmpty()
                            ? documentClass.getSimpleName()
                            : annotation.beanName();

                    String searchBeanName = "searchService" + baseName;

                    if (!registry.containsBeanDefinition(searchBeanName)) {
                        log.info("✅ Registering search service bean for @EnableSearchable document: {}", documentClass.getSimpleName());
                        registerBeanForDocument(registry, documentClass, searchBeanName);
                    }
                }
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load class: {}", candidate.getBeanClassName(), e);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(@NonNull ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Rien à faire ici
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
     * Enregistre le bean SearchService<T> pour un document
     */
    private void registerBeanForDocument(
            BeanDefinitionRegistry registry,
            Class<?> documentClass,
            String beanName) {

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(SearchService.class);
        beanDefinition.setInstanceSupplier(() -> factoryProvider.createSearchService(documentClass));
        beanDefinition.setLazyInit(true);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
