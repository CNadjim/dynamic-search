package io.github.cnadjim.dynamic.search.spring.jpa.processor;

import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.service.SearchService;
import io.github.cnadjim.dynamic.search.spring.jpa.factory.SearchServiceFactoryProvider;
import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.BeanDefinitionRegistryPostProcessor;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;

import java.util.Set;

/**
 * Processor responsable de scanner les entités JPA annotées @EnableSearchable
 * et d'enregistrer les beans SearchUseCase<T> et GetAvailableFiltersUseCase<T>.
 *
 * Utilise BeanDefinitionRegistryPostProcessor pour scanner le classpath et enregistrer
 * les beans avant la création des autres beans.
 * Les beans sont enregistrés avec un instanceSupplier lazy qui appelle le factory.
 */
@Slf4j
public class SearchableEntityRegistrationProcessor implements BeanDefinitionRegistryPostProcessor {

    private final SearchServiceFactoryProvider factoryProvider;

    public SearchableEntityRegistrationProcessor(SearchServiceFactoryProvider factoryProvider) {
        this.factoryProvider = factoryProvider;
    }

    @Override
    public void postProcessBeanDefinitionRegistry(BeanDefinitionRegistry registry) throws BeansException {
        log.info("🔍 Scanning for @EnableSearchable JPA entities...");

        // Scanner le classpath complet pour trouver les classes annotées @Entity et @EnableSearchable
        ClassPathScanningCandidateComponentProvider scanner = createScanner();

        // Scanner en partant de la racine (tous les packages)
        Set<BeanDefinition> candidates = scanner.findCandidateComponents("");

        for (BeanDefinition candidate : candidates) {
            try {
                Class<?> entityClass = Class.forName(candidate.getBeanClassName());

                // Vérifier que la classe a bien les deux annotations
                if (entityClass.isAnnotationPresent(Entity.class) &&
                    entityClass.isAnnotationPresent(EnableSearchable.class)) {

                    EnableSearchable annotation = entityClass.getAnnotation(EnableSearchable.class);

                    String baseName = annotation.beanName().isEmpty()
                            ? entityClass.getSimpleName()
                            : annotation.beanName();

                    String searchBeanName = "searchService" + baseName;

                    if (!registry.containsBeanDefinition(searchBeanName)) {
                        log.info("✅ Registering search service bean for @EnableSearchable entity: {}", entityClass.getSimpleName());
                        registerBeanForEntity(registry, entityClass, searchBeanName);
                    }
                }
            } catch (ClassNotFoundException e) {
                log.warn("Failed to load class: {}", candidate.getBeanClassName(), e);
            }
        }
    }

    @Override
    public void postProcessBeanFactory(ConfigurableListableBeanFactory beanFactory) throws BeansException {
        // Rien à faire ici
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
     * Enregistre le bean SearchService<T> pour une entité
     */
    private void registerBeanForEntity(
            BeanDefinitionRegistry registry,
            Class<?> entityClass,
            String beanName) {

        GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
        beanDefinition.setBeanClass(SearchService.class);
        beanDefinition.setInstanceSupplier(() -> factoryProvider.createSearchService(entityClass));
        beanDefinition.setLazyInit(true);
        registry.registerBeanDefinition(beanName, beanDefinition);
    }
}
