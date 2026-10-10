# Automated Streetlight Fault Reporting System

## Project Overview

A complete web application for citizens and municipal staff to report, track, and resolve streetlight faults, built with Spring Boot 3.x, Thymeleaf, MySQL 8, and Docker. This project also includes a full DevOps lifecycle with Jenkins CI/CD, Selenium testing, and Ansible provisioning.

## Table of Contents

- [Documentation](#documentation)
- [Prerequisites](#prerequisites)
- [Quick Start](#quick-start)
- [Running the Application](#running-the-application)
- [Testing](#testing)
- [Docker Deployment](#docker-deployment)
- [CI/CD Pipeline](#cicd-pipeline)
- [Ansible Provisioning](#ansible-provisioning)
- [Viva Preparation](#viva-preparation)

## Prerequisites

- Java 21 JDK
- Maven 3.9.x (or use `mvnw` wrapper)
- Docker Desktop or Docker Engine + Compose
- MySQL 8.0
- Git
- Chrome Browser (for Selenium tests)
- Node.js (optional, for frontend tools)

## Quick Start

```bash
# 1. Clone the repository
git clone https://github.com/your-username/streetlight-fault-reporting.git
cd streetlight-fault-reporting

# 2. Initialize environment
cp .env.example .env
# Edit .env with your values (not committed to Git)

# 3. Start the application
docker compose up -d

# 4. Access the application
# - http://localhost:8080 (direct)
# - http://localhost (via Nginx reverse proxy)

# 5. Default login credentials (change on first login):
#   - Admin: admin / admin123
#   - Technician: technician1 / tech123
#   - Citizen: citizen1 / citizen123
```

## Running the Application

### Local Development

```bash
# Using Maven Wrapper
./mvnw spring-boot:run

# Or with Maven installed
mvn spring-boot:run
```

### Docker Development

```bash
docker compose -f docker-compose.yml up -d
# Or with test profile
docker compose -f docker-compose.test.yml up -d
```

### With Nginx Reverse Proxy

```bash
docker compose up -d  # starts app + mysql + nginx
```

## Testing

### Unit Tests

```bash
./mvnw test
```

### Integration Tests

```bash
./mvnw test -Dspring.profiles.active=test
```

### Selenium Tests

```bash
./mvnw test -Dtest="*SeleniumTest"
```

## Docker Deployment

### Development

```bash
docker compose up -d
```

### Production

```bash
# With environment variables
DB_PASSWORD=mysecurepassword docker compose up -d
```

### Rollback

```bash
# Rollback to previous image tag
docker pull streetlight/fault-reporting:v1.0.0
docker tag streetlight/fault-reporting:v1.0.0 streetlight/fault-reporting:latest
docker compose up -d
```

## CI/CD Pipeline

The project uses Jenkins with a 12-stage pipeline:

1. Checkout
2. Environment and Java version verification
3. Maven clean and compile
4. Unit tests
5. Integration tests
6. Package the application
7. Archive the JAR artifact
8. Build a Docker image
9. Run Selenium tests against deployed app
10. Publish test reports
11. Deploy only if all quality gates pass
12. Deployment health check

Triggered on Git commits via GitHub webhook or polling.

## Ansible Provisioning

```bash
# First deployment (makes changes)
ansible-playbook -i ansible\inventory.yml ansible\site.yml

# Second run (idempotent - should show changed=0)
ansible-playbook -i ansible\inventory.yml ansible\site.yml
```

Directory structure:
- `ansible\inventory.yml` - Target host configuration
- `ansible\vars.yml` - Variable values (placeholders)
- `ansible\site.yml` - Main playbook orchestrating roles
- `ansible\roles\system` - Package installation, directories
- `ansible\roles\docker` - Docker installation, service management
- `ansible\roles\application` - Copy configs, deploy app
- `ansible\roles\health` - Health checks, verification

## Viva Preparation

Key topics to prepare:
- Architecture and layered design
- Role-based authorization with Spring Security
- Status workflow enforcement
- Database design with Flyway migrations
- CI/CD pipeline stages and quality gates
- Docker containerization and health checks
- Ansible idempotency and rollback procedures

Common viva questions are documented in `docs/viva-questions.md`.