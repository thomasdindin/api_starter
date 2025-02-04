# Projet de base pour une API REST
Ce projet sert de base pour une api. 
Il contient une implémentation solide de l'architecture REST, des tests unitaires et d'intégration, et une documentation Swagger.

## TODO
- Revoir la réponse de l'authentification : renvoyer l'id pour envoyer le lien de vérification de l'email, et le JWT serait suffisant. 
- Revoir l'anti DDoS : même si l'ip est bloquée, on peut quand même effectuer des requêtes
## Prérequis
- docker
- java 23
- maven 3.8.4
- git

## Installation
```bash
git clone
cd
mvn clean install
```

Pour pull l'image docker si vous ne l'avez pas déjà :
```bash
docker pull postgres:latest
```

Pour lancer le container docker :
```bash
docker-compose up -d
```

Pour lancer RabbitMQ : 
```bash
docker run -d --hostname my-rabbit --name some-rabbit -p 5672:5672 -p 15672:15672 rabbitmq:3-management
```

Pour lancer MailHog : 
```bash
docker run -d -p 1025:1025 -p 8025:8025 mailhog/mailhog
```

## Utilisation
```bash
mvn spring-boot:run
```

## Documentation
