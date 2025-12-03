# Changelog

Toutes les modifications notables de ce projet seront documentées dans ce fichier.

Le format est basé sur [Keep a Changelog](https://keepachangelog.com/fr/1.0.0/),
et ce projet adhère à [Semantic Versioning](https://semver.org/lang/fr/).

## [Non publié]

## [0.0.1-SNAPSHOT] - 2025-12-03

### Ajouté

#### Architecture
- ✅ Architecture hexagonale (Ports & Adapters) complète
- ✅ Séparation stricte domaine / infrastructure
- ✅ Module domain indépendant sans dépendances techniques

#### Fonctionnalités Principales
- ✅ Support **JPA** (SQL) - PostgreSQL, MySQL, H2
- ✅ Support **MongoDB** (NoSQL)
- ✅ **13 opérateurs de filtrage** :
  - `equals`, `notEquals`
  - `contains`, `notContains`
  - `startsWith`, `endsWith`
  - `in`, `notIn`
  - `lessThan`, `greaterThan`
  - `between`
  - `blank`, `notBlank`
- ✅ **Tri dynamique** (ASC/DESC)
- ✅ **Pagination** configurable
- ✅ **SearchGateway** - Façade simplifiée pour l'API

#### Gestion des Dates
- ✅ Support multi-formats de dates :
  - ISO 8601 : `2024-12-03T10:00:00` et `2024-12-03`
  - Format SQL : `2024-12-03 10:00:00`
  - Formats européens : `03-12-2024 HH:mm:ss` et `03/12/2024 HH:mm:ss`
- ✅ **Conversion automatique EQUALS → BETWEEN** pour les dates sans heure
  - `releaseDate equals "2024-12-03"` devient automatiquement `between 2024-12-03T00:00:00 and 2024-12-03T23:59:59.999999999`

#### Détection Automatique
- ✅ **Déduction automatique du fieldType** depuis les métadonnées d'entité
- ✅ Plus besoin de spécifier explicitement le type dans les requêtes
- ✅ EntityMetadataStorage pour centraliser les métadonnées

#### Auto-configuration Spring Boot
- ✅ Configuration automatique pour JPA
- ✅ Configuration automatique pour MongoDB
- ✅ Enregistrement dynamique des beans SearchService
- ✅ Annotation `@EnableSearchable` pour activer la recherche sur une entité

#### Documentation
- ✅ README complet avec exemples
- ✅ ARCHITECTURE.md détaillant les patterns et flux
- ✅ Documentation OpenAPI (Swagger) intégrée
- ✅ Commentaires javadoc exhaustifs

#### Tests et Performance
- ✅ DataInitializer pour générer 1M d'enregistrements de test
- ✅ Tests de performance validés sur 1M records
- ✅ Support H2 pour tests en mémoire

### Modifié

#### Refactoring
- ✅ Suppression de `FieldTypeResolver` (duplication avec `EntityMetadataStorage.resolveFieldType()`)
- ✅ Factorisation de `FieldTypeParser` dans le module domain
  - Suppression des doublons dans JPA et MongoDB starters
  - Utilisation de `java.util.logging.Logger` au lieu de Lombok @Slf4j
- ✅ Simplification de `SearchRequestMapper` pour utiliser directement `EntityMetadataStorage`
- ✅ Optimisation de `DefaultSearchGateway` en supprimant la couche intermédiaire

#### Amélioration du Code
- ✅ Réduction de la duplication de code
- ✅ Meilleure séparation des responsabilités
- ✅ Code plus maintenable et testable

### Technique

#### Stack Technologique
- Java 21
- Spring Boot 3.5.0
- Spring Data JPA
- Spring Data MongoDB
- Maven multi-modules
- Lombok (sauf module domain)
- Jakarta Persistence API
- OpenAPI / Swagger

#### Structure des Modules
```
dynamic-search/
├── dynamic-search-domain                    # Logique métier pure
├── dynamic-search-spring-boot-starter       # Contrats REST et configuration
├── dynamic-search-spring-boot-jpa-starter   # Implémentation JPA
├── dynamic-search-spring-boot-mongo-starter # Implémentation MongoDB
└── dynamic-search-spring-boot-example       # Exemple d'utilisation
```

#### Design Patterns Implémentés
- Hexagonal Architecture (Ports & Adapters)
- Specification Pattern (JPA)
- Builder Pattern (MongoDB)
- Adapter Pattern (JPA & MongoDB)
- Façade Pattern (SearchGateway)
- Factory Pattern (SearchServiceFactoryProvider)
- Strategy Pattern (EntityRepository)

### Corrections de Bugs

#### Parsing de Dates
- 🐛 **Corrigé** : Erreur "Cannot compare left expression of type 'java.time.LocalDateTime' with right expression of type 'java.lang.String'"
  - Ajout du support multi-formats dans FieldTypeParser

#### Recherche par Date
- 🐛 **Corrigé** : Recherche EQUALS avec date seule ne retournait aucun résultat
  - Implémentation de la conversion automatique vers BETWEEN

#### Duplication de Code
- 🐛 **Corrigé** : FieldTypeParser dupliqué dans JPA et MongoDB starters
  - Factorisation dans le module domain

- 🐛 **Corrigé** : FieldTypeResolver dupliquait la logique de EntityMetadataStorage
  - Suppression de FieldTypeResolver

### Connu (Limitations)

#### Fonctionnalités à Venir
- ⏳ Support Elasticsearch (prévu pour v0.1.0)
- ⏳ Support des opérateurs OR entre filtres (actuellement uniquement AND)
- ⏳ Support des filtres imbriqués (nested objects)
- ⏳ Cache des résultats de recherche
- ⏳ Agrégations et statistiques

#### Limitations Techniques
- Les filtres sont toujours combinés avec AND (pas de support OR)
- Pas de support des relationsMany-To-Many dans les filtres
- MongoDB : les regex sont case-insensitive par défaut

### Sécurité

- ✅ Validation des paramètres d'entrée
- ✅ Protection contre les injections SQL (via JPA Criteria)
- ✅ Protection contre les injections NoSQL (via MongoDB Criteria)
- ⚠️ Pas encore d'authentification/autorisation intégrée (à gérer au niveau application)

### Performance

#### Benchmarks (1M records, H2 en mémoire)
- Recherche simple (1 filtre) : ~50-100ms
- Recherche complexe (5 filtres) : ~100-200ms
- Recherche avec tri : ~150-250ms
- Recherche avec pagination : ~50-100ms

*Note : Les performances dépendent fortement des index définis sur la base de données*

### Migration

Aucune migration nécessaire - première version.

---

## Format des Entrées

### [X.Y.Z] - YYYY-MM-DD

#### Ajouté
Nouvelles fonctionnalités.

#### Modifié
Changements dans les fonctionnalités existantes.

#### Déprécié
Fonctionnalités qui seront bientôt supprimées.

#### Supprimé
Fonctionnalités supprimées.

#### Corrigé
Corrections de bugs.

#### Sécurité
En cas de vulnérabilités.

---

[Non publié]: https://github.com/cnadjim/dynamic-search/compare/v0.0.1-SNAPSHOT...HEAD
[0.0.1-SNAPSHOT]: https://github.com/cnadjim/dynamic-search/releases/tag/v0.0.1-SNAPSHOT
