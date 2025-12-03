package io.github.cnadjim.dynamic.search.spring.mongo.autoconfigure;

import io.github.cnadjim.dynamic.search.spring.mongo.config.SearchableMongoBeanRegistrar;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.context.annotation.Import;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.repository.MongoRepository;

/**
 * Auto-configuration pour le starter MongoDB avec recherche dynamique
 *
 * Cette configuration active automatiquement le registrar qui scannera
 * les documents MongoDB annotés @EnableSearchable et créera les beans nécessaires
 *
 * Usage:
 * 1. Annoter votre document avec @EnableSearchable
 * 2. Annoter les champs filtrables avec @Searchable
 * 3. Les beans SearchUseCase<T> et GetAvailableFiltersUseCase<T> sont créés automatiquement
 */
@Slf4j
@AutoConfiguration
@ConditionalOnClass({Document.class, MongoRepository.class, MongoTemplate.class})
@Import(SearchableMongoBeanRegistrar.class)
public class DynamicSearchMongoAutoConfiguration {

    public DynamicSearchMongoAutoConfiguration() {
        log.info("✅ DynamicSearchMongoAutoConfiguration activated - Ready to scan @EnableSearchable documents");
    }

}
