# Keycloak Configuration
This directory contains Keycloak realm configuration files for different environments.

## Files
- **configfyrealm.json**: Main configuration for local development
- **configfyrealm-test.json**: Simplified configuration for automated testing

## Pre-configured Users (Development)
### testuser
- **Username**: `testuser`
- **Password**: `testpass`
- **Email**: testuser@example.com
- **Roles**: user, developer
- **Usage**: Test basic functionality

### admin
- **Username**: `admin`
- **Password**: `admin`
- **Email**: admin@example.com
- **Roles**: user, admin, developer
- **Usage**: Test administrative functions

### viewer
- **Username**: `viewer`
- **Password**: `viewer`
- **Email**: viewer@example.com
- **Roles**: user, viewer
- **Usage**: Test read-only access

## Configured Clients
### configfy-api
- **Client ID**: `configfy-api`
- **Type**: Confidential
- **Secret**: JjcwF10P38swenKq9s4gLY752aLjQp6y
- **Use**: Backend REST API

### configfy-web
- **Client ID**: `configfy-web`
- **Type**: Public (PKCE enabled)
- **Use**: React frontend dashboard

### configfy-sdk
- **Client ID**: `configfy-sdk`
- **Type**: Bearer Only
- **Use**: SDK integrations

## Roles
### Realm Roles
- **user**: Basic authenticated user
- **admin**: System administrator

### Client Roles (configfy-api)
- **developer**: Can create and manage feature flags
- **admin**: Full administrative access
- **viewer**: Read-only analytics and flags access

## Groups
- **/Developers**: Developers (role: developer)
- **/Admins**: Administrators (role: admin)
- **/Viewers**: Viewers (role: viewer)

## Import
The realm is automatically imported via Docker Compose:

```bash
docker-compose up keycloak
```
## Access to Admin Console
- **URL**: http://localhost:8081
- **Admin Username**: admin
- **Admin Password**: admin

## Get JWT Token (Sample)
```bash
curl -X POST http://localhost:8081/realms/configfyrealm/protocol/openid-connect/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "client_id=configfy-api" \
  -d "client_secret=JjcwF10P38swenKq9s4gLY752aLjQp6y" \
  -d "username=testuser" \
  -d "password=testpass"
  ```
## Modification Notes
⚠️ **IMPORTANT**: After making changes in the Keycloak admin console, export the realm:

Go to Admin Console

Realm Settings → Partial Export

Select "Export groups and roles" and "Export clients"

Save JSON and overwrite the corresponding file

## Security
⚠️ **DO NOT USE THESE CONFIGURATIONS IN PRODUCTION**

All passwords are for development convenience

Client secrets must be regenerated

Activate HTTPS for production

Configure SMTP for real emails

Properly enable brute force protection