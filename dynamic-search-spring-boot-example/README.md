# Dynamic Search - Exemple Multi-Technologie

Exemple d'utilisation de la bibliothèque **Dynamic Search** avec support de **3 technologies** de persistance :
- ✅ **PostgreSQL** (JPA)
- ✅ **MongoDB** (NoSQL)
- ✅ **Elasticsearch** (Search Engine)

## 🎯 Fonctionnalités

- **6 endpoints REST** : `/jpa`, `/mongo`, `/elastic` avec `/filters` et `/search`
- **Recherche dynamique** : Filtrage, tri, pagination, **full-text search**
- **Frontend React** avec AG Grid Infinite Row Model
- **1000 enregistrements** générés automatiquement dans les 3 bases
- **Architecture propre** : Model + 3 Mappers vers JPA/Mongo/Elastic

## 🚀 Démarrage Rapide

### 1. Démarrer l'infrastructure (Docker)

```bash
docker-compose up -d
```

Cela démarre :
- **PostgreSQL** sur le port `5432`
- **MongoDB** sur le port `27017`
- **Elasticsearch** sur le port `9200`

Vérifier que tout fonctionne :
```bash
docker-compose ps
```

### 2. Compiler et lancer l'application

```bash
# Compiler (inclut le build du frontend React)
mvn clean package

# Lancer l'application
java -jar target/dynamic-search-spring-boot-jpa-example-0.0.1-SNAPSHOT.jar
```

Ou en développement :
```bash
mvn spring-boot:run
```

### 3. Accéder à l'application

- **Frontend** : http://localhost:8080
- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **API Base URL** : http://localhost:8080/api/operating-systems

## 📡 API Endpoints

### JPA (PostgreSQL)

```bash
# Récupérer les filtres disponibles
GET /api/operating-systems/jpa/filters

# Recherche dynamique
POST /api/operating-systems/jpa/search
```

### MongoDB

```bash
# Récupérer les filtres disponibles
GET /api/operating-systems/mongo/filters

# Recherche dynamique
POST /api/operating-systems/mongo/search
```

### Elasticsearch

```bash
# Récupérer les filtres disponibles
GET /api/operating-systems/elastic/filters

# Recherche dynamique
POST /api/operating-systems/elastic/search
```

## 🔍 Exemple de Requête avec Full-Text

```json
POST /api/operating-systems/mongo/search

{
  "fullText": {
    "query": "Ubuntu"
  },
  "filters": [
    {
      "key": "usages",
      "operator": "greaterThan",
      "value": "500000"
    }
  ],
  "sorts": [
    {
      "key": "releaseDate",
      "direction": "desc"
    }
  ],
  "page": {
    "number": 0,
    "size": 20
  }
}
```

## 🏗️ Architecture du Projet

```
src/main/java/
├── config/
│   └── DataInitializer.java           # Initialise les 3 bases avec 1000 enregistrements
├── controller/
│   └── OperatingSystemController.java # 6 endpoints (jpa/mongo/elastic)
├── entity/
│   ├── jpa/
│   │   └── OperatingSystemJpaEntity.java
│   ├── mongo/
│   │   └── OperatingSystemMongoDocument.java
│   └── elastic/
│       └── OperatingSystemElasticDocument.java
├── mapper/
│   ├── OperatingSystemJpaMapper.java       # Model ↔ JPA
│   ├── OperatingSystemMongoMapper.java     # Model ↔ Mongo
│   └── OperatingSystemElasticMapper.java   # Model ↔ Elastic
└── model/
    └── OperatingSystemModel.java      # Modèle domaine canonique
```

## ⚙️ Configuration

Fichier `application.yml` :

```yaml
app:
  data:
    init:
      enabled: true  # Active l'initialisation des données
      size: 1000     # Nombre d'enregistrements à générer
```

## 🛠️ Développement Frontend

Le frontend utilise React + TypeScript + Vite + AG Grid.

```bash
cd src/main/resources/webapp

# Installer les dépendances
npm install

# Lancer en mode dev (hot reload)
npm run dev
# Accessible sur http://localhost:5173

# Build pour la production (copié dans target/classes/static)
npm run build
```

## 🧹 Nettoyage

Pour arrêter et supprimer les conteneurs Docker :

```bash
docker-compose down

# Supprimer aussi les volumes (données)
docker-compose down -v
```

## 📊 Comparaison des Technologies

| Fonctionnalité | JPA (PostgreSQL) | MongoDB | Elasticsearch |
|----------------|------------------|---------|---------------|
| Type | Relationnel | NoSQL Document | Search Engine |
| Transactions | ✅ ACID | ❌ Limité | ❌ Non |
| Recherche Full-Text | ⚠️ Basique | ✅ Regex | ✅✅ Natif |
| Scalabilité Horizontale | ❌ Complexe | ✅ Sharding | ✅ Sharding |
| Requêtes Complexes | ✅ SQL/JPQL | ✅ Aggregations | ✅ DSL |

## 📚 Documentation

- [Dynamic Search Library](../README.md)
- [Swagger UI](http://localhost:8080/swagger-ui.html) (après démarrage)

## 🐛 Troubleshooting

### Ports déjà utilisés

Si les ports 5432, 27017 ou 9200 sont déjà utilisés :

1. Modifier les ports dans `docker-compose.yml`
2. Modifier les URLs dans `application.yml`

### Elasticsearch ne démarre pas

Augmenter la mémoire allouée :

```yaml
# docker-compose.yml
elasticsearch:
  environment:
    - "ES_JAVA_OPTS=-Xms1g -Xmx1g"  # Au lieu de 512m
```

### Données non initialisées

Vérifier les logs au démarrage. Si l'initialisation échoue :

```bash
# Nettoyer et redémarrer
docker-compose down -v
docker-compose up -d
mvn spring-boot:run
```

## 📝 TODO Frontend

- [ ] Ajouter Shadcn/ui Tabs pour basculer entre JPA/Mongo/Elastic
- [ ] Implémenter le changement d'endpoint en fonction du tab sélectionné
- [ ] Ajouter un indicateur de performance (temps de réponse par technologie)
- [ ] Ajouter un champ de recherche full-text dans l'interface

## 🤝 Contribution

Pour ajouter d'autres technologies de persistance :

1. Créer l'entité/document correspondante dans `entity/`
2. Créer le mapper dans `mapper/`
3. Ajouter 2 endpoints dans le contrôleur
4. Ajouter la configuration dans `application.yml`
5. Mettre à jour le `DataInitializer`
