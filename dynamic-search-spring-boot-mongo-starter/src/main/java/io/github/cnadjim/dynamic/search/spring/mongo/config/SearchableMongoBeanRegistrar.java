package io.github.cnadjim.dynamic.search.spring.mongo.config;

import io.github.cnadjim.dynamic.search.spring.mongo.factory.SearchServiceFactoryProvider;
import io.github.cnadjim.dynamic.search.spring.mongo.processor.SearchableDocumentRegistrationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * Configuration Spring pour l'enregistrement automatique des beans SearchUseCase et GetAvailableFiltersUseCase
 * pour les documents MongoDB annotés @EnableSearchable
 *
 * Utilise ImportBeanDefinitionRegistrar pour enregistrer :
 * 1. SearchServiceFactoryProvider - Factory pour créer les SearchService
 * 2. SearchableDocumentRegistrationProcessor - Processor qui scanne et enregistre les documents
 */
@Slf4j
@Configuration
public class SearchableMongoBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        log.info("🔍 SearchableMongoBeanRegistrar - Registering factory and processor...");

        // Enregistrer le Factory Provider
        registerFactoryProvider(registry);

        // Enregistrer le Registration Processor
        registerProcessor(registry);
    }

    /**
     * Enregistre le SearchServiceFactoryProvider
     */
    private void registerFactoryProvider(BeanDefinitionRegistry registry) {
        String beanName = generateBeanName(SearchServiceFactoryProvider.class.getName());

        if (registry.containsBeanDefinition(beanName)) {
            log.debug("Bean {} already registered, skipping", beanName);
            return;
        }

        try {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(SearchServiceFactoryProvider.class);
            beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_CONSTRUCTOR);
            registry.registerBeanDefinition(beanName, beanDefinition);
            log.debug("✓ Registered bean: {}", beanName);
        } catch (Exception exception) {
            log.error("Failed to register bean: {}", beanName, exception);
        }
    }

    /**
     * Enregistre le SearchableDocumentRegistrationProcessor
     */
    private void registerProcessor(BeanDefinitionRegistry registry) {
        String beanName = generateBeanName(SearchableDocumentRegistrationProcessor.class.getName());

        if (registry.containsBeanDefinition(beanName)) {
            log.debug("Bean {} already registered, skipping", beanName);
            return;
        }

        try {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(SearchableDocumentRegistrationProcessor.class);

            // Fournir le factory provider comme argument du constructeur
            String factoryBeanName = generateBeanName(SearchServiceFactoryProvider.class.getName());
            beanDefinition.getConstructorArgumentValues().addGenericArgumentValue(new RuntimeBeanReference(factoryBeanName));

            registry.registerBeanDefinition(beanName, beanDefinition);
            log.debug("✓ Registered bean: {}", beanName);
        } catch (Exception exception) {
            log.error("Failed to register bean: {}", beanName, exception);
        }
    }

    private String generateBeanName(String className) {
        String simpleName = className.substring(className.lastIndexOf('.') + 1);
        return Character.toLowerCase(simpleName.charAt(0)) + simpleName.substring(1);
    }
}
