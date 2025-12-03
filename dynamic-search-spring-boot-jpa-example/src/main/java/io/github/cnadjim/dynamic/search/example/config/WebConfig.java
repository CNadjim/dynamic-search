package io.github.cnadjim.dynamic.search.example.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Configuration Web pour servir l'interface React
 * Redirige toutes les requêtes non-API vers index.html pour le routing côté client
 */
@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addViewControllers(ViewControllerRegistry registry) {
        // Rediriger la racine vers index.html
        registry.addViewController("/").setViewName("forward:/index.html");
    }
}
