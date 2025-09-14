# WladLwe9t - Guide Docker

## Prérequis

- Docker Desktop installé et démarré
- Java 21 pour le développement local

## Lancement avec Docker Compose

### 1. Construction et démarrage complet

```bash
# Construire l'application Java
mvn clean package -DskipTests

# Démarrer tous les services (PostgreSQL + Application)
docker-compose up -d

# Vérifier les logs
docker-compose logs -f
```

### 2. Démarrage étape par étape

```bash
# Démarrer uniquement PostgreSQL
docker-compose up postgres -d

# Attendre que PostgreSQL soit prêt
docker-compose logs postgres

# Démarrer l'application
docker-compose up app -d
```

### 3. Vérification du fonctionnement

```bash
# Health check
curl http://localhost:8080/api/status

# Test des produits (avec authentification)
curl -u tintin:acrobate http://localhost:8080/api/produits
```

### 4. Arrêt des services

```bash
# Arrêter tous les services
docker-compose down

# Arrêter et supprimer les volumes (données perdues)
docker-compose down -v
```

## Configuration

- **Base de données**: PostgreSQL 15
  - Host: `postgres` (dans Docker) / `localhost` (local)
  - Port: 5432
  - Database: `wladlwe9t`
  - User: `wladlwe9t_user`
  - Password: `wladlwe9t_password`

- **Application**: Spring Boot
  - Port: 8080
  - Profile: `docker` (automatiquement activé)
  - Context path: `/api`

## Liquibase

Les migrations sont automatiquement exécutées au démarrage de l'application :

1. Création de la table `products`
2. Insertion des données initiales (5 produits)

## Sécurité

- Authentification HTTP Basic
- Utilisateur: `tintin` / `acrobate`
- Endpoint `/status` public pour health checks

## Développement local (alternative)

Si vous préférez développer sans Docker :

1. Installer PostgreSQL localement
2. Créer la base `wladlwe9t` avec l'utilisateur `wladlwe9t_user`
3. Lancer l'application avec le profil par défaut : `mvn spring-boot:run`