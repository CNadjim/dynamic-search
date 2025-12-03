# Dynamic Search - Exemple JPA avec Interface React

Exemple d'application Spring Boot démontrant l'utilisation de la bibliothèque `dynamic-search` avec JPA et une interface utilisateur React avec AG Grid.

## 🎯 Fonctionnalités

- ✅ API REST de recherche dynamique avec Spring Boot
- ✅ Interface web React avec AG Grid
- ✅ Filtrage dynamique en temps réel
- ✅ Base de données H2 en mémoire
- ✅ Build automatisé frontend + backend

## 🚀 Démarrage Rapide

### Prérequis

- Java 21
- Maven 3.8+
- Node.js 20+ (pour le développement)

### Build et Exécution

```bash
# Build complet (backend + frontend)
mvn clean package

# Lancer l'application
java -jar target/dynamic-search-spring-boot-jpa-example-0.0.1-SNAPSHOT.jar

# Ou directement avec Maven
mvn spring-boot:run
```

L'application sera accessible sur :
- **Interface web** : http://localhost:8080
- **API REST** : http://localhost:8080/api/operating-systems
- **Swagger UI** : http://localhost:8080/swagger-ui.html
- **H2 Console** : http://localhost:8080/h2-console

## 📊 Données d'Exemple

L'application charge automatiquement des données de systèmes d'exploitation au démarrage :
- Windows, Linux, macOS, Android, iOS, etc.
- Avec dates de sortie, parts de marché, et statut Open Source

## 🔧 Développement

### Développement Frontend

```bash
cd src/main/resources/webapp

# Installer les dépendances
npm install

# Lancer le serveur de dev Vite
npm run dev
```

Le serveur de développement Vite sera accessible sur http://localhost:5173 et proxyfiera les appels API vers http://localhost:8080.

### Développement Backend

```bash
mvn spring-boot:run
```

## 📝 API Endpoints

### Récupérer les filtres disponibles
```
GET /api/operating-systems/filters
```

Retourne la liste des champs filtrables avec leurs types et opérateurs supportés.

### Recherche dynamique
```
POST /api/operating-systems/search
Content-Type: application/json

{
  "filters": [
    {
      "key": "name",
      "operator": "CONTAINS",
      "value": "Windows"
    },
    {
      "key": "marketShare",
      "operator": "GREATER_THAN",
      "value": 5
    }
  ],
  "sorts": [
    {
      "key": "name",
      "direction": "ASC"
    }
  ],
  "page": {
    "number": 0,
    "size": 20
  }
}
```

## 🏗️ Structure du Projet

```
dynamic-search-spring-boot-jpa-example/
├── src/main/
│   ├── java/.../
│   │   ├── config/
│   │   │   ├── DataInitializer.java        # Initialisation des données
│   │   │   └── WebConfig.java              # Configuration Web
│   │   ├── controller/
│   │   │   └── OperatingSystemController.java  # API REST
│   │   └── entity/
│   │       └── OperatingSystemEntity.java  # Entité JPA
│   └── resources/
│       ├── static/                         # Fichiers statiques (générés)
│       ├── webapp/                         # Application React
│       │   ├── src/
│       │   │   ├── components/
│       │   │   │   └── OperatingSystemGrid.tsx
│       │   │   ├── services/
│       │   │   │   └── api.ts
│       │   │   └── types/
│       │   │       └── api.ts
│       │   ├── package.json
│       │   └── vite.config.ts
│       └── application.properties
```

## 🔍 Opérateurs Disponibles

- `EQUALS` - Égalité exacte
- `NOT_EQUALS` - Différent de
- `CONTAINS` - Contient (chaîne)
- `NOT_CONTAINS` - Ne contient pas
- `STARTS_WITH` - Commence par
- `ENDS_WITH` - Se termine par
- `LESS_THAN` - Inférieur à
- `GREATER_THAN` - Supérieur à
- `IN` - Dans la liste
- `NOT_IN` - Pas dans la liste
- `BETWEEN` - Entre deux valeurs
- `BLANK` - Vide ou null
- `NOT_BLANK` - Non vide

## 🛠️ Technologies Utilisées

### Backend
- Spring Boot 3.5.0
- Spring Data JPA
- H2 Database
- Lombok
- SpringDoc OpenAPI

### Frontend
- React 18
- TypeScript
- Vite
- AG Grid Community
- CSS3

## 📦 Build Maven

Le build Maven automatise complètement la construction du frontend :

1. **Installation de Node.js** - Via frontend-maven-plugin
2. **Installation des dépendances npm** - `npm install`
3. **Build du frontend** - `npm run build`
4. **Copie dans resources/static** - Les fichiers sont copiés automatiquement
5. **Packaging du JAR** - Tout est inclus dans le JAR final

Le JAR final contient à la fois le backend Spring Boot et le frontend React compilé.
