# Projet de base pour une API REST
Ce projet sert de base pour une api. 
Il contient une implémentation solide de l'architecture REST, des tests unitaires et d'intégration, et une documentation Swagger.

## TODO
- [ ] Créer les tests unitaires et d'intégration pour le service d'authentification
- [ ] Créer une documentation Swagger

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


## Utilisation
```bash
mvn spring-boot:run
```

## Documentation
