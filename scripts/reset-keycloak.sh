#!/bin/bash
echo "🔄 Reseting Keycloak..."

docker-compose stop keycloak

docker-compose exec postgres psql -U postgres -d configfydb -c "DROP SCHEMA IF EXISTS keycloak CASCADE;"

docker-compose up -d keycloak

echo "✅ Keycloak erased and restarted! Wait ~60s for initialization..."
