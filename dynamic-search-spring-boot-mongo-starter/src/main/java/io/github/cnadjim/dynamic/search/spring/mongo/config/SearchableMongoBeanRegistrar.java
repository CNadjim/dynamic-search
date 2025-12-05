package io.github.cnadjim.dynamic.search.spring.mongo.config;

import io.github.cnadjim.dynamic.search.spring.mongo.processor.SearchableMongoDocumentRegistrationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * Configuration Spring pour l'enregistrement automatique du processor qui scanne
 * les documents MongoDB annotés @EnableSearchable et les enregistre auprès du SearchService.
 *
 * Utilise ImportBeanDefinitionRegistrar pour enregistrer :
 * - SearchableDocumentRegistrationProcessor : Processor qui scanne et enregistre les documents
 */
@Slf4j
@Configuration
public class SearchableMongoBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        log.info("🔍 SearchableMongoBeanRegistrar - Registering document registration processor...");

        // Enregistrer le Registration Processor
        registerProcessor(registry);
    }

    /**
     * Enregistre le SearchableMongoDocumentRegistrationProcessor
     * Injecte SearchService et MongoTemplate via autowiring
     */
    private void registerProcessor(BeanDefinitionRegistry registry) {
        String beanName = generateBeanName(SearchableMongoDocumentRegistrationProcessor.class.getName());

        if (registry.containsBeanDefinition(beanName)) {
            log.debug("Bean {} already registered, skipping", beanName);
            return;
        }

        try {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(SearchableMongoDocumentRegistrationProcessor.class);

            // Injecter SearchService et MongoTemplate comme arguments du constructeur
            beanDefinition.setAutowireMode(GenericBeanDefinition.AUTOWIRE_CONSTRUCTOR);

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
