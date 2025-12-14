# API de Trésorerie - Backend

Ce projet est le service backend pour un module de gestion de trésorerie d'un système ERP. Il est construit avec une architecture moderne, réactive et non-bloquante en utilisant Java et Spring Boot.

## 1. Description Générale

L'API de trésorerie fournit les fonctionnalités nécessaires pour gérer les opérations financières de base, y compris la gestion des comptes bancaires, le suivi des transactions, le rapprochement bancaire et l'audit des activités.

Le système est conçu pour être robuste et performant, en utilisant des technologies réactives pour gérer un grand volume d'opérations de manière efficace.

## 2. Fonctionnalités Principales

Le backend expose une API RESTful pour les fonctionnalités suivantes :

*   **Gestion des Référentiels :**
    *   CRUD pour les `banques`.
    *   CRUD pour les `types de transaction`.

*   **Gestion de la Trésorerie :**
    *   CRUD pour les `comptes bancaires` de l'entreprise.
    *   Gestion complète du cycle de vie des `transactions bancaires` internes (Brouillon -> Validé / Annulé).
    *   Suivi des `chèques` émis et reçus.

*   **Rapprochement Bancaire :**
    *   Importation des `relevés bancaires` et de leurs `lignes de relevé`.
    *   Rapprochement (automatique et manuel) entre les lignes de relevé et les transactions internes ou les chèques.

*   **Audit et Sécurité :**
    *   Journalisation complète de toutes les actions des utilisateurs (Création, Modification, Validation, etc.) sur les entités principales.
    *   Stockage des états avant/après modification pour une traçabilité complète.

## 3. Technologies et Architecture

Ce projet est construit sur une stack technique moderne et performante :

*   **Langage :** Java 17
*   **Framework :** Spring Boot 3.2.x
*   **Web :** Spring WebFlux (programmation réactive)
*   **Accès aux Données :** R2DBC (Reactive Relational Database Connectivity)
*   **Base de Données :** PostgreSQL
*   **Migrations de Base de Données :** Liquibase
*   **Documentation API :** SpringDoc (OpenAPI v3)
*   **Build Tool :** Apache Maven

L'architecture est basée sur le principe de séparation des couches (Controller, Service, Repository) et utilise l'injection de dépendances de Spring.

## 4. Schéma de la Base de Données

La structure de la base de données est gérée par Liquibase et est organisée autour des concepts suivants :

*   `banks`, `transaction_types` : Données de référence.
*   `bank_accounts` : Comptes bancaires de l'entreprise.
*   `bank_transactions` : Transactions internes.
*   `checks` : Chèques.
*   `bank_statements`, `statement_lines` : Données d'import pour le rapprochement.
*   `reconciliation_matches` : Table de lien pour le rapprochement.
*   `audit_logs` : Table d'audit de toutes les opérations.

Les scripts de migration se trouvent dans `src/main/resources/db/changelog/`.

## 5. Démarrage Rapide

### Prérequis

*   Java 17 ou supérieur
*   Apache Maven 3.6+
*   Une instance PostgreSQL en cours d'exécution
*   (Optionnel) Docker pour lancer facilement une base de données

### Lancement

1.  **Configurer la base de données :**
    Assurez-vous que votre instance PostgreSQL est accessible. Mettez à jour les informations de connexion (URL, utilisateur, mot de passe) dans le fichier `src/main/resources/application.yml`.

    ```yaml
    spring:
      r2dbc:
        url: r2dbc:postgresql://localhost:5432/treasury_db
        username: your_username
        password: your_password
    ```

2.  **Lancer l'application :**
    Utilisez Maven pour compiler et démarrer l'application.

    ```bash
    mvn spring-boot:run
    ```

3.  **Consulter la documentation de l'API :**
    Une fois l'application démarrée, la documentation Swagger UI est disponible à l'adresse suivante :
    [http://localhost:8080/swagger-ui.html](http://localhost:8080/swagger-ui.html)
