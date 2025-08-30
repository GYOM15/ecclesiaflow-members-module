# 👥 EcclesiaFlow Members Module

[![Java](https://img.shields.io/badge/Java-21-orange.svg)](https://openjdk.java.net/projects/jdk/21/)
[![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.1-brightgreen.svg)](https://spring.io/projects/spring-boot)
[![License](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)
[![Build Status](https://img.shields.io/badge/Build-Passing-success.svg)]()

> **Module de gestion des membres pour la plateforme de gestion d'église EcclesiaFlow**

Un service de gestion des membres robuste et sécurisé conçu pour supporter l'architecture multi-tenant d'EcclesiaFlow, permettant la gestion complète du cycle de vie des membres d'église, de l'inscription à la confirmation de compte.

## 📋 Table des matières

- [🎯 Vue d'ensemble](#-vue-densemble)
- [🏗️ Architecture](#️-architecture)
- [🚀 Démarrage rapide](#-démarrage-rapide)
- [📚 API Documentation](#-api-documentation)
- [🔧 Configuration](#-configuration)
- [🛡️ Sécurité](#️-sécurité)
- [🧪 Tests](#-tests)
- [📦 Déploiement](#-déploiement)
- [🤝 Contribution](#-contribution)

## 🎯 Vue d'ensemble

### Objectif du module

Ce module fournit les services de gestion des membres pour l'écosystème EcclesiaFlow :

- **Gestion des membres** - CRUD complet pour les profils membres
- **Système de confirmation** - Validation par email avec codes temporaires
- **Gestion des mots de passe** - Changement et réinitialisation sécurisés
- **Notifications email** - Communication automatisée avec les membres
- **Support multi-tenant** - Architecture prête pour la distribution

### Fonctionnalités principales

✅ **Inscription des membres** avec validation email  
✅ **Confirmation de compte** par code temporaire  
✅ **Gestion des profils** avec mise à jour sécurisée  
✅ **Système de mots de passe** avec hachage sécurisé  
✅ **Notifications email** automatiques  
✅ **API RESTful** complètement documentée  
✅ **Validation des données** stricte  
✅ **Gestion des erreurs** centralisée  

### Architecture cible

```
┌─────────────────────────────────────────────────────────────┐
│                    SUPER ADMIN                              │
├─────────────────────────────────────────────────────────────┤
│  TENANT 1 (Église A)    │  TENANT 2 (Église B)    │ ...    │
│  ┌─────────────────────┐ │ ┌─────────────────────┐  │        │
│  │ Pastor (Admin)      │ │ │ Pastor (Admin)      │  │        │
│  │ ├─ Member 1         │ │ │ ├─ Member 1         │  │        │
│  │ ├─ Member 2         │ │ │ ├─ Member 2         │  │        │
│  │ └─ ...              │ │ │ └─ ...              │  │        │
│  └─────────────────────┘ │ └─────────────────────┘  │        │
└─────────────────────────────────────────────────────────────┘
```

## 🏗️ Architecture

### Stack technologique

- **Java 21** - LTS avec les dernières fonctionnalités
- **Spring Boot 3.2.1** - Framework principal
- **Spring Data JPA** - Persistance des données
- **MySQL** - Base de données relationnelle
- **Spring Mail** - Envoi d'emails
- **Lombok** - Réduction du code boilerplate
- **SpringDoc OpenAPI** - Documentation automatique
- **Maven** - Gestion des dépendances

### Principes architecturaux appliqués

- ✅ **Clean Architecture** - Séparation claire des couches
- ✅ **SOLID Principles** - Code maintenable et extensible
- ✅ **Domain-Driven Design** - Logique métier centralisée
- ✅ **AOP (Aspect-Oriented Programming)** - Logging transversal
- ✅ **API-First Design** - Documentation OpenAPI complète
- ✅ **Exception Handling** - Gestion centralisée des erreurs

### Structure du projet

```
src/
├── main/
│   ├── java/com/ecclesiaflow/
│   │   ├── business/            # Logique métier
│   │   │   ├── aspect/          # Aspects AOP (logging)
│   │   │   ├── domain/          # Objets métier
│   │   │   ├── mappers/         # Conversion entités/DTO
│   │   │   └── services/        # Services métier
│   │   ├── io/                  # Couche d'accès aux données
│   │   │   ├── annotation/      # Annotations personnalisées
│   │   │   ├── entities/        # Entités JPA
│   │   │   └── repository/      # Repositories JPA
│   │   └── web/                 # Couche présentation
│   │       ├── config/          # Configuration Spring
│   │       ├── controller/      # Contrôleurs REST
│   │       ├── dto/             # Data Transfer Objects
│   │       └── exception/       # Gestion des exceptions
│   └── resources/
│       ├── api/                 # Spécifications OpenAPI
│       └── application.properties
└── test/                        # Tests unitaires et d'intégration
```

## 🚀 Démarrage rapide

### Prérequis

- Java 21 ou supérieur
- Maven 3.8+
- MySQL 8.0+
- Compte Gmail avec App Password (pour l'envoi d'emails)
- IDE compatible (IntelliJ IDEA recommandé)

### Installation

1. **Cloner le repository**
```bash
git clone https://github.com/ecclesiaflow/ecclesiaflow-members-module.git
cd ecclesiaflow-members-module
```

2. **Configurer la base de données**
```sql
CREATE DATABASE spring_security;
CREATE USER 'ecclesiaflow'@'localhost' IDENTIFIED BY 'your_password';
GRANT ALL PRIVILEGES ON spring_security.* TO 'ecclesiaflow'@'localhost';
```

3. **Configurer les variables d'environnement**
```bash
# Copier le fichier d'exemple
cp .env.example .env

# Éditer les variables
vim .env
```

4. **Configurer l'email Gmail**
   - Activer l'authentification à 2 facteurs sur votre compte Gmail
   - Générer un App Password dans : Google Account > Security > 2-Step Verification > App passwords
   - Mettre à jour `application.properties` avec vos informations

5. **Lancer l'application**
```bash
mvn spring-boot:run
```

L'application sera accessible sur `http://localhost:8080`

### Premiers tests

```bash
# Vérifier que l'application fonctionne
curl http://localhost:8080/actuator/health

# Accéder à la documentation Swagger
open http://localhost:8080/swagger-ui.html
```

## 📚 API Documentation

### Endpoints principaux

| Endpoint | Méthode | Description | Auth requise |
|----------|---------|-------------|--------------|
| `/members` | GET     | Liste des membres | Oui |
| `/members` | POST    | Créer un nouveau membre | Oui |
| `/members/{memberId}` | GET     | Détails d'un membre | Oui |
| `/members/{memberId}` | PATCH   | Mettre à jour un membre | Oui |
| `/members/{memberId}` | DELETE  | Supprimer un membre | Oui |
| `/members/{memberId}/confirmation/send` | POST    | Envoyer code de confirmation | Oui |
| `/members/{memberId}/confirmation/resend` | POST    | Renvoyer code de confirmation | Oui |
| `/members/{memberId}/confirmation/verify` | POST    | Vérifier code de confirmation | Oui |
| `/members/{memberId}/password/change` | POST    | Changer le mot de passe | Oui |
| `/members/{memberId}/password/set` | POST    | Définir le mot de passe | Oui |

### Content Types supportés

- **v1**: `application/vnd.ecclesiaflow.members.v1+json`
- **v2**: `application/vnd.ecclesiaflow.members.v2+json`

### Exemples d'utilisation

**Créer un nouveau membre :**
```bash
curl -X POST http://localhost:8080/members \
  -H "Content-Type: application/json" \
  -H "Accept: application/vnd.ecclesiaflow.members.v2+json" \
  -H "Authorization: Bearer YOUR_JWT_TOKEN" \
  -d '{
    "firstName": "Jean",
    "lastName": "Dupont",
    "email": "jean.dupont@example.com",
    "address": "123 Rue Trol, Montréal, H5V 3H6"
  }'
```

**Envoyer un code de confirmation :**
```bash
curl -X POST http://localhost:8080/members/123e4567-e89b-12d3-a456-426614174000/confirmation \
  -H "Content-Type: application/json" \
  -H "Accept: application/vnd.ecclesiaflow.members.v2+json" \
  
```

**Vérifier un code de confirmation :**
```bash
curl -X POST http://localhost:8080/members/123e4567-e89b-12d3-a456-426614174000/confirmation/verify \
  -H "Content-Type: application/json" \
  -H "Accept: application/vnd.ecclesiaflow.members.v2+json" \
  -d '{
    "Code": "123456"
  }'
```

### Documentation interactive

- **Swagger UI** : `http://localhost:8080/swagger-ui/index.html`
- **OpenAPI Spec** : `http://localhost:8080/v3/api-docs`
- **Fichiers YAML** : `src/main/resources/api/`

## 🔧 Configuration

### Variables d'environnement principales

```bash
# Base de données
DB_HOST=localhost
DB_PORT=3306
DB_NAME=nom_bd
DB_USERNAME=username_bd
DB_PASSWORD=mot_de_passe_bd

# JWT Configuration (pour l'intégration avec l'auth module)
JWT_SECRET=404E635266556A586E3272357538782F413F4428472B4B6250645367566B5970
JWT_TOKEN_EXPIRATION=86400000  # 24 heures
JWT_REFRESH_TOKEN_EXPIRATION=604800000  # 7 jours

# Configuration Email - Gmail
MAIL_HOST=smtp.gmail.com
MAIL_PORT=587
MAIL_USERNAME=your-email@gmail.com
MAIL_PASSWORD=your-app-password  # App Password Gmail
MAIL_FROM=your-email@gmail.com

# Auth Module Integration
AUTH_MODULE_BASE_URL=http://localhost:8081
```

### Configuration Email Gmail

1. **Activer l'authentification à 2 facteurs** sur votre compte Gmail
2. **Générer un App Password** :
   - Aller dans Google Account > Security > 2-Step Verification > App passwords
   - Sélectionner "Mail" et votre appareil
   - Utiliser le mot de passe généré (16 caractères) dans `MAIL_PASSWORD`

### Profils Spring

- **`dev`** - Développement local avec logs debug
- **`test`** - Tests automatisés avec base H2
- **`prod`** - Production avec MySQL et logs optimisés

```bash
# Lancer avec un profil spécifique
mvn spring-boot:run -Dspring-boot.run.profiles=dev
```

## 🛡️ Sécurité

### Fonctionnalités de sécurité

- **🔐 JWT Integration** - Authentification via module auth centralisé
- **📧 Email Verification** - Confirmation par code temporaire
- **🔒 Password Security** - Validation et hachage sécurisés
- **✅ Input Validation** - Validation stricte des données d'entrée
- **🛡️ Exception Handling** - Gestion sécurisée des erreurs
- **📝 Audit Logging** - Traçabilité des opérations critiques

### Validation des données

```java
// Exemple de validation des emails
@Email(message = "Format d'email invalide")
@NotBlank(message = "L'email est requis")
private String email;

// Validation des mots de passe
@Size(min = 8, message = "Le mot de passe doit contenir au moins 8 caractères")
@Pattern(regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d).*$", 
         message = "Le mot de passe doit contenir au moins une majuscule, une minuscule et un chiffre")
private String password;
```

### Bonnes pratiques appliquées

- ✅ Validation des entrées utilisateur
- ✅ Codes de confirmation temporaires (expiration)
- ✅ Logging des tentatives de confirmation
- ✅ Gestion centralisée des exceptions
- ✅ Intégration sécurisée avec le module auth

## 🧪 Tests

### Lancer les tests

```bash
# Tests unitaires
mvn test

# Tests d'intégration
mvn verify

# Tests avec couverture
mvn clean test jacoco:report
```

### Structure des tests

```
src/test/java/
├── unit/                    # Tests unitaires
│   ├── services/
│   ├── controllers/
│   └── mappers/
├── integration/             # Tests d'intégration
│   ├── api/
│   ├── repository/
│   └── mail/
└── fixtures/                # Données de test
```

### Exemples de tests

```java
@Test
@DisplayName("Devrait créer un nouveau membre avec succès")
void shouldCreateMemberSuccessfully() {
    // Given
    SignUpRequest request = new SignUpRequest();
    request.setEmail("test@example.com");
    request.setFirstName("Test");
    request.setLastName("User");
    
    // When & Then
    mockMvc.perform(post("/members")
            .contentType(MediaType.APPLICATION_JSON)
            .content(objectMapper.writeValueAsString(request)))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.email").value("test@example.com"));
}
```

## 📦 Déploiement

### Build de production

```bash
# Créer le JAR
mvn clean package -Pprod

# Le JAR sera dans target/
ls target/ecclesiaflow-members-module-*.jar
```

### Docker

```dockerfile
FROM openjdk:21-jre-slim

# Créer un utilisateur non-root
RUN groupadd -r ecclesiaflow && useradd -r -g ecclesiaflow ecclesiaflow

# Copier le JAR
COPY target/ecclesiaflow-members-module-*.jar app.jar

# Changer propriétaire
RUN chown ecclesiaflow:ecclesiaflow app.jar

# Utiliser l'utilisateur non-root
USER ecclesiaflow

# Exposer le port
EXPOSE 8080

# Point d'entrée
ENTRYPOINT ["java", "-jar", "/app.jar"]
```

```bash
# Build et run
docker build -t ecclesiaflow-members .
docker run -p 8080:8080 -e SPRING_PROFILES_ACTIVE=prod ecclesiaflow-members
```

### Docker Compose

```yaml
version: '3.8'
services:
  members-module:
    build: .
    ports:
      - "8080:8080"
    environment:
      - SPRING_PROFILES_ACTIVE=prod
      - DB_HOST=mysql
      - DB_USERNAME=ecclesiaflow
      - DB_PASSWORD=${DB_PASSWORD}
    depends_on:
      - mysql
    
  mysql:
    image: mysql:8.0
    environment:
      MYSQL_DATABASE: spring_security
      MYSQL_USER: ecclesiaflow
      MYSQL_PASSWORD: ${DB_PASSWORD}
      MYSQL_ROOT_PASSWORD: ${MYSQL_ROOT_PASSWORD}
    volumes:
      - mysql_data:/var/lib/mysql
    
volumes:
  mysql_data:
```

## 🤝 Contribution

### Workflow de développement

1. **Fork** le repository
2. **Créer** une branche feature (`git checkout -b feature/amazing-feature`)
3. **Commit** vos changements (`git commit -m 'Add amazing feature'`)
4. **Push** vers la branche (`git push origin feature/amazing-feature`)
5. **Ouvrir** une Pull Request

### Standards de code

- **Commits atomiques** avec messages conventionnels
- **Tests** pour toute nouvelle fonctionnalité
- **Documentation** mise à jour (OpenAPI, README)
- **Code review** obligatoire
- **Couverture de tests** > 80%

### Messages de commit

```
feat(members): add member profile update endpoint
fix(email): resolve confirmation code generation issue
docs(api): update OpenAPI specification for v2
refactor(services): improve password validation logic
test(integration): add member confirmation flow tests
```

### Règles de développement

- ✅ Utiliser Lombok pour réduire le boilerplate
- ✅ Documenter les endpoints avec OpenAPI
- ✅ Valider toutes les entrées utilisateur
- ✅ Gérer les exceptions de manière centralisée
- ✅ Écrire des tests pour chaque nouvelle fonctionnalité
- ✅ Suivre les conventions de nommage Java

## 🐛 Dépannage

### Problèmes courants

**Erreur 500 au démarrage :**
- Vérifier la configuration de la base de données
- S'assurer que MySQL est démarré
- Contrôler les logs d'application

**Emails non envoyés :**
- Vérifier l'App Password Gmail
- Contrôler la configuration SMTP
- Vérifier les logs de Spring Mail

**Erreurs de JWT :**
- S'assurer que le module auth est démarré
- Vérifier la configuration JWT_SECRET
- Contrôler la validité des tokens

### Logs utiles

```bash
# Logs de débogage des services
logging.level.com.ecclesiaflow.business.services=DEBUG

# Logs des emails
logging.level.org.springframework.mail=DEBUG

# Logs des contrôleurs
logging.level.com.ecclesiaflow.web.controller=DEBUG
```

## 📄 Licence

Ce projet est sous licence MIT. Voir le fichier [LICENSE](LICENSE) pour plus de détails.

## 🔗 Liens utiles

- [Documentation Spring Boot](https://spring.io/projects/spring-boot)
- [Guide Spring Data JPA](https://spring.io/guides/gs/accessing-data-jpa/)
- [Documentation Spring Mail](https://docs.spring.io/spring-framework/docs/current/reference/html/integration.html#mail)
- [OpenAPI Specification](https://swagger.io/specification/)
- [Gmail App Passwords](https://support.google.com/accounts/answer/185833)

## 🏢 Modules connexes

- **[EcclesiaFlow Auth Module](../ecclesiaflow-auth-module)** - Authentification centralisée

---

**Développé avec ❤️ pour la communauté EcclesiaFlow**

> 📞 **Support** : dev@ecclesiaflow.com  
> 🌐 **Site web** : https://ecclesiaflow.com  
> 📚 **Documentation** : https://docs.ecclesiaflow.com
