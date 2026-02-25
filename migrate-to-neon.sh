#!/bin/bash
# =============================================================================
# Script pour exécuter les migrations Liquibase vers Neon
# =============================================================================
# Usage: ./migrate-to-neon.sh
# =============================================================================

echo "=== Migration Liquibase vers Neon ==="
echo ""

# Demander les informations de connexion (endpoint DIRECT, pas pooler!)
echo "IMPORTANT: Utilisez l'endpoint DIRECT de Neon (sans '-pooler' dans l'URL)"
echo ""

read -p "Host Neon (ex: ep-shy-wind-abc123.eu-central-1.aws.neon.tech): " NEON_HOST
read -p "Nom de la base (ex: neondb): " NEON_DB
read -p "Utilisateur (ex: neondb_owner): " NEON_USER
read -s -p "Mot de passe: " NEON_PASSWORD
echo ""

echo ""
echo "Connexion à: $NEON_HOST / $NEON_DB"
echo "Exécution des migrations..."
echo ""

# Exécuter les migrations avec Maven
NEON_HOST=$NEON_HOST \
NEON_DB=$NEON_DB \
NEON_USER=$NEON_USER \
NEON_PASSWORD=$NEON_PASSWORD \
mvn spring-boot:run -Dspring-boot.run.profiles=migrate -DskipTests

echo ""
echo "=== Migration terminée ==="
