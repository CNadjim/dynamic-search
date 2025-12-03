package io.github.cnadjim.dynamic.search.spring.elasticsearch.autoconfigure;

import io.github.cnadjim.dynamic.search.spring.elasticsearch.config.SearchableElasticsearchBeanRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

/**
 * Auto-configuration pour le starter Elasticsearch avec recherche dynamique
 *
 * Cette configuration active automatiquement le registrar qui scannera
 * les documents Elasticsearch annotés @EnableSearchable et créera les beans nécessaires
 *
 * Usage:
 * 1. Annoter votre document avec @EnableSearchable
 * 2. Annoter les champs filtrables avec @Searchable
 * 3. Les beans SearchUseCase<T> et GetAvailableFiltersUseCase<T> sont créés automatiquement
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({ElasticsearchRepository.class, ElasticsearchOperations.class})
@Import(SearchableElasticsearchBeanRegistrar.class)
public class DynamicSearchElasticsearchAutoConfiguration {

    public DynamicSearchElasticsearchAutoConfiguration() {
        log.info("✅ DynamicSearchElasticsearchAutoConfiguration activated - Ready to scan @EnableSearchable documents");
    }

}
