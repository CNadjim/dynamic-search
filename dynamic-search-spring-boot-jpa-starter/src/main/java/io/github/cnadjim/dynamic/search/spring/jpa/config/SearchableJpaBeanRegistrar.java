package io.github.cnadjim.dynamic.search.spring.jpa.config;

import io.github.cnadjim.dynamic.search.spring.jpa.processor.SearchableEntityRegistrationProcessor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.config.RuntimeBeanReference;
import org.springframework.beans.factory.support.BeanDefinitionRegistry;
import org.springframework.beans.factory.support.GenericBeanDefinition;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.ImportBeanDefinitionRegistrar;
import org.springframework.core.type.AnnotationMetadata;
import org.springframework.lang.NonNull;

/**
 * Configuration Spring pour l'enregistrement automatique du processor qui scanne
 * les entités JPA annotées @EnableSearchable et les enregistre auprès du SearchService.
 *
 * Utilise ImportBeanDefinitionRegistrar pour enregistrer :
 * - SearchableEntityRegistrationProcessor : Processor qui scanne et enregistre les entités
 */
@Slf4j
@Configuration
public class SearchableJpaBeanRegistrar implements ImportBeanDefinitionRegistrar {

    @Override
    public void registerBeanDefinitions(@NonNull AnnotationMetadata importingClassMetadata, @NonNull BeanDefinitionRegistry registry) {
        log.info("🔍 SearchableJpaBeanRegistrar - Registering entity registration processor...");

        // Enregistrer le Registration Processor
        registerProcessor(registry);
    }

    /**
     * Enregistre le SearchableEntityRegistrationProcessor
     * Injecte SearchService et EntityManager via des références de beans
     */
    private void registerProcessor(BeanDefinitionRegistry registry) {
        String beanName = generateBeanName(SearchableEntityRegistrationProcessor.class.getName());

        if (registry.containsBeanDefinition(beanName)) {
            log.debug("Bean {} already registered, skipping", beanName);
            return;
        }

        try {
            GenericBeanDefinition beanDefinition = new GenericBeanDefinition();
            beanDefinition.setBeanClass(SearchableEntityRegistrationProcessor.class);

            // Injecter SearchService et EntityManager comme arguments du constructeur
            // EntityManager sera injecté via autowiring depuis l'EntityManagerFactory
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
