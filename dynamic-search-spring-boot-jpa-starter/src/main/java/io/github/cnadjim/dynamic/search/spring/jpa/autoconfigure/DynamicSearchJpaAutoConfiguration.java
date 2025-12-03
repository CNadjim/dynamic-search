package io.github.cnadjim.dynamic.search.spring.jpa.autoconfigure;

import io.github.cnadjim.dynamic.search.spring.jpa.config.SearchableJpaBeanRegistrar;
import jakarta.persistence.Entity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * Auto-configuration pour le starter JPA avec recherche dynamique
 *
 * Cette configuration active automatiquement le registrar qui scannera
 * les entités JPA annotées @EnableSearchable et créera les beans nécessaires
 *
 * Usage:
 * 1. Annoter votre entité avec @EnableSearchable
 * 2. Annoter les champs filtrables avec @Searchable
 * 3. Les beans SearchUseCase<T> et GetAvailableFiltersUseCase<T> sont créés automatiquement
 */
@Slf4j
@AutoConfiguration
@AutoConfigureAfter(HibernateJpaAutoConfiguration.class)
@ConditionalOnClass({Entity.class, JpaRepository.class})
@Import(SearchableJpaBeanRegistrar.class)
public class DynamicSearchJpaAutoConfiguration {

    public DynamicSearchJpaAutoConfiguration() {
        log.info("✅ DynamicSearchJpaAutoConfiguration activated - Ready to scan @EnableSearchable entities");
    }

}
