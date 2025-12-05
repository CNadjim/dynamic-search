package io.github.cnadjim.dynamic.search.example;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.data.mongodb.repository.config.EnableMongoRepositories;

/**
 * Application Spring Boot d'exemple - Démonstration de spring-dynamic-search
 * Expose une API REST pour rechercher des systèmes d'exploitation avec filtrage dynamique
 */
@SpringBootApplication
@OpenAPIDefinition(info = @Info(title = "${spring.application.name}"))
public class ExampleApplication {

    public static void main(String[] args) {
        SpringApplication.run(ExampleApplication.class, args);
    }
}
