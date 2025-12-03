# Dynamic Search - Elasticsearch Starter

Module d'intégration Elasticsearch pour le framework Dynamic Search. Ce starter permet d'utiliser Elasticsearch comme backend de recherche avec toutes les fonctionnalités de recherche dynamique.

## Fonctionnalités

- 🔍 Recherche dynamique avec Elasticsearch
- 📊 Pagination et tri
- 🎯 Filtrage avancé (égalité, comparaison, contains, in, between, etc.)
- 🏗️ Architecture hexagonale
- ⚡ Auto-configuration Spring Boot
- 🔧 Configuration par annotations

## Installation

Ajoutez la dépendance dans votre `pom.xml` :

```xml
<dependency>
    <groupId>io.github.cnadjim</groupId>
    <artifactId>dynamic-search-spring-boot-elasticsearch-starter</artifactId>
    <version>0.0.1-SNAPSHOT</version>
</dependency>
```

## Configuration

### 1. Configuration Elasticsearch

Configurez la connexion Elasticsearch dans `application.yml` :

```yaml
spring:
  elasticsearch:
    uris: http://localhost:9200
    username: elastic
    password: changeme
```

### 2. Annoter votre Document

```java
import org.springframework.data.elasticsearch.annotations.Document;
import io.github.cnadjim.dynamic.search.annotation.EnableSearchable;
import io.github.cnadjim.dynamic.search.annotation.Searchable;

@Document(indexName = "products")
@EnableSearchable(beanName = "Product")
public class Product {

    @Searchable(label = "Nom", type = FieldType.STRING)
    private String name;

    @Searchable(label = "Prix", type = FieldType.NUMBER)
    private Double price;

    @Searchable(label = "Catégorie", type = FieldType.STRING)
    private String category;

    @Searchable(label = "En stock", type = FieldType.BOOLEAN)
    private Boolean inStock;

    // Getters et setters
}
```

### 3. Utiliser le Service de Recherche

Le bean `SearchService<Product>` est automatiquement créé :

```java
@RestController
@RequestMapping("/api/products")
public class ProductController {

    private final SearchService<Product> searchService;

    public ProductController(@Qualifier("searchServiceProduct") SearchService<Product> searchService) {
        this.searchService = searchService;
    }

    @PostMapping("/search")
    public SearchResult<Product> search(@RequestBody SearchRequest request) {
        SearchCriteria criteria = SearchRequestMapper.toCriteria(request);
        return searchService.search(criteria);
    }

    @GetMapping("/filters")
    public EntityDescriptor getFilters() {
        return searchService.getAvailableFilters();
    }
}
```

## Opérateurs Supportés

| Opérateur | Description | Types supportés |
|-----------|-------------|-----------------|
| `EQUALS` | Égalité exacte | Tous |
| `NOT_EQUALS` | Différent de | Tous |
| `LESS_THAN` | Inférieur à | NUMBER, DATE |
| `GREATER_THAN` | Supérieur à | NUMBER, DATE |
| `CONTAINS` | Contient (case insensitive) | STRING |
| `NOT_CONTAINS` | Ne contient pas | STRING |
| `STARTS_WITH` | Commence par | STRING |
| `ENDS_WITH` | Termine par | STRING |
| `IN` | Dans la liste | Tous |
| `NOT_IN` | Pas dans la liste | Tous |
| `BETWEEN` | Entre deux valeurs | NUMBER, DATE |
| `BLANK` | Vide ou null | Tous |
| `NOT_BLANK` | Non vide | Tous |

## Types de Champs

- `STRING` : Chaînes de caractères
- `NUMBER` : Nombres (Integer, Long, Double, Float)
- `DATE` : Dates (LocalDate, LocalDateTime)
- `BOOLEAN` : Booléens

## Exemple de Requête

```json
{
  "filters": [
    {
      "key": "name",
      "operator": "CONTAINS",
      "value": "laptop"
    },
    {
      "key": "price",
      "operator": "BETWEEN",
      "value": 500,
      "valueTo": 1500
    },
    {
      "key": "inStock",
      "operator": "EQUALS",
      "value": true
    }
  ],
  "page": {
    "number": 0,
    "size": 20
  },
  "sorts": [
    {
      "key": "price",
      "direction": "ASC"
    }
  ]
}
```

## Architecture

```
dynamic-search-spring-boot-elasticsearch-starter/
├── adapter/
│   └── ElasticsearchEntityRepositoryAdapter.java
├── autoconfigure/
│   └── DynamicSearchElasticsearchAutoConfiguration.java
├── config/
│   └── SearchableElasticsearchBeanRegistrar.java
├── criteria/
│   └── ElasticsearchCriteriaBuilder.java
├── factory/
│   └── SearchServiceFactoryProvider.java
└── processor/
    └── SearchableDocumentRegistrationProcessor.java
```

## Différences avec MongoDB

Ce starter fonctionne de manière similaire au starter MongoDB, mais avec quelques différences liées à Elasticsearch :

1. **Recherche Full-Text** : Elasticsearch offre de meilleures capacités de recherche textuelle
2. **Performance** : Optimisé pour les grandes volumétries et recherches complexes
3. **Scoring** : Possibilité d'obtenir un score de pertinence pour chaque résultat
4. **Analyse** : Support natif de l'analyse de texte et tokenization

## Notes Techniques

- Utilise l'Elasticsearch Java Client (co.elastic.clients)
- Compatible avec Elasticsearch 8.x
- Support de Spring Data Elasticsearch
- Architecture hexagonale avec ports et adaptateurs

## Voir Aussi

- [Module Domain](../dynamic-search-domain/README.md)
- [Starter Commun](../dynamic-search-spring-boot-starter/README.md)
- [Starter JPA](../dynamic-search-spring-boot-jpa-starter/README.md)
- [Starter MongoDB](../dynamic-search-spring-boot-mongo-starter/README.md)
