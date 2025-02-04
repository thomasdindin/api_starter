# API Starter – Base pour une API REST sécurisée et évolutive

Le projet **API Starter** est un template d'API REST construit avec Spring Boot. Il offre une architecture modulaire et maintenable en intégrant :

- **Sécurité et Authentification JWT**  
  Gère l'inscription, la connexion (avec génération de token et refresh token), la vérification d'email, et le blocage de compte après trop de tentatives infructueuses.

- **Gestion des Utilisateurs et des Rôles**  
  Chaque utilisateur possède un rôle (défini via une enum, par défaut `CLIENT`); les comptes administrateurs pourront être définis manuellement en base ou via une interface dédiée.

- **Envoi Asynchrone d'Emails**  
  Utilisation de RabbitMQ pour envoyer des emails de vérification et de réinitialisation de mot de passe de façon asynchrone.  
  MailHog est utilisé en développement pour visualiser les emails envoyés.

- **Base de Données et Migrations**  
  PostgreSQL est utilisé comme base de données, avec les migrations gérées par Flyway.

- **Documentation**  
  L’API est documentée avec Swagger, facilitant ainsi son exploration et ses tests.

---

## Prérequis

- **Docker** – pour lancer PostgreSQL, RabbitMQ et MailHog
- **Java 20** (ou la version indiquée dans le `pom.xml`)
- **Maven 3.8.4** (ou ultérieur)
- **Git**

---

## Installation

### 1. Clonage du projet

```bash
git clone <URL_DU_REPOSITORY>
cd api_starter
```

### 2. Compilation et packaging
    
```bash
mvn clean install
```

## Lancement des services Docker

Dans le répertoire racine du projet, exécutez la commande suivante :

```bash
docker-compose up -d
```

Ce fichier démarre : 

- **PostgreSQL** sur le port `5432`
- **RabbitMQ** sur le port `5672` et le port `15672` pour l'interface web
- **MailHog** sur le port `8025` pour visualiser les emails envoyés

## Lancer l'API

Pour lancer l'API en mode développement, éxecutez : 
    
```bash
mvn spring-boot:run
```

## Fonctionnalités 

### Authentification et utilisateurs
- Inscription d'un utilisateur
- Connexion d'un utilisateur
- Vérification d'email
- Récupération de mot de passe

### Utilisateurs et rôles
- JWT
- Rôles gérés par CustomUserDetailsService

### Envoi d'emails
- Envoi asynchrone d'emails avec RabbitMQ
- MailHog pour visualiser les emails envoyés

### Base de données
- PostgreSQL
- Flyway pour les migrations