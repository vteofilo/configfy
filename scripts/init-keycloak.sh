#!/bin/bash

KEYCLOAK_HOST="keycloak"
KEYCLOAK_PORT=8080
TIMEOUT=60
ELAPSED=0

REALM="configfyrealm"
CLIENT="configfy-api"
ADMIN_USER="admin"
ADMIN_PASSWORD="adminpassword"

echo "## Waiting Keycloak start ${KEYCLOAK_HOST}:${KEYCLOAK_PORT}..."

until nc -z $KEYCLOAK_HOST $KEYCLOAK_PORT; do
  sleep 2
  ELAPSED=$((ELAPSED + 2))
  if [ $ELAPSED -ge $TIMEOUT ]; then
    echo "Timeout: Keycloak not answered after ${TIMEOUT}s."
    exit 1
  fi
done

until nc -z postgres 5432; do
  echo "Waiting Postgres initialization..."
  sleep 2
done

echo "## Keycloak available, resuming configuration..."

/opt/keycloak/bin/kcadm.sh config credentials --server http://${KEYCLOAK_HOST}:${KEYCLOAK_PORT}/auth --realm master --user $ADMIN_USER --password $ADMIN_PASSWORD

/opt/keycloak/bin/kcadm.sh create realms -s realm=$REALM -s enabled=true

/opt/keycloak/bin/kcadm.sh create clients -r $REALM -s clientId=$CLIENT -s enabled=true -s publicClient=false -s directAccessGrantsEnabled=true

/opt/keycloak/bin/kcadm.sh create users -r $REALM -s username=testuser -s enabled=true
/opt/keycloak/bin/kcadm.sh set-password -r $REALM --username testuser --new-password testpass

echo "## Keycloak configuration succeeded!"
