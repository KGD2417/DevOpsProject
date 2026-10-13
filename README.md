# Automated Streetlight Fault Reporting System

A Java 21 / Spring Boot application for citizens to report streetlight faults and for municipal staff to triage, assign, track, and resolve them. The UI uses Thymeleaf and Bootstrap; MySQL stores data, Flyway manages schema changes, and Spring Security enforces role-based access.

## Run locally with Docker

Requirements: Docker Desktop with Compose enabled.

```powershell
Copy-Item .env.example .env
docker compose up --build -d
docker compose ps
```

Open [http://localhost:8080](http://localhost:8080) or [http://localhost:8081](http://localhost:8081) through Nginx. The app connects to an internal MySQL service. Edit `.env` to change local credentials or the Nginx port. Do not commit `.env` or use the demo accounts in a production environment.

The `local` profile creates demo users and a sample report when the database is empty:

| Role | Username | Password |
|---|---|---|
| Admin | `admin` | `admin123` |
| Technician | `technician1` | `tech123` |
| Citizen | `citizen1` | `citizen123` |

Citizens can also create an account from the sign-in page. Demo seeding is disabled in production profiles. The first database start runs `V1__initial_schema.sql` through Flyway.

To stop the services while keeping database data, run `docker compose down`. To remove the database volume too, run `docker compose down -v`.

## Main workflows

- Citizens register, report a fault with location, ward, fault type, priority, and description, then view their reports and status history.
- Administrators see the system dashboard, search reports, assign technicians, edit or delete reports, and reopen resolved or closed reports.
- Technicians see reports assigned to them and move them through the repair workflow, adding resolution notes when resolving.
- Valid status transitions are enforced by the service: reported → acknowledged → assigned → in progress → resolved → closed.

## Local development

Requirements: JDK 21 and Maven 3.9+. Start MySQL separately, then set `DB_HOST`, `DB_PORT`, `DB_NAME`, `DB_USER`, and `DB_PASSWORD` as needed.

```powershell
mvn spring-boot:run
```

The default profile is `local`.

## Verification

```powershell
mvn clean verify
```

The test profile uses an in-memory H2 database. `docker compose -f docker-compose.test.yml up --build -d` starts the app with the test profile for browser-driven checks; stop it with the corresponding `down` command. Selenium tests are tagged `e2e` and are excluded from the default Maven run; run them with `mvn -Dgroups=e2e test` after starting the test app and configuring a supported browser.

## Deployment and CI

The Docker image is built with `docker build -f src/main/docker/Dockerfile -t streetlight/fault-reporting:local .`. Configure real credentials outside source control before deploying. Production must use a non-demo profile, strong database credentials, TLS termination, and managed backups.

The Jenkins pipeline checks out the configured SCM, runs Maven verification, builds the image, and can publish it when the `PUBLISH_IMAGE` parameter is enabled and the Jenkins credential `docker-hub-credentials` is configured. It does not automatically deploy to production.

Ansible deployment notes are in [docs/ansible-guide.md](docs/ansible-guide.md); requirements and design detail are in the [docs](docs/architecture.md) folder.

## Project layout

```text
src/main/java/       Spring MVC, security, persistence, services
src/main/resources/  configuration, Thymeleaf views, CSS, Flyway migrations
src/test/java/       browser-based end-to-end scenarios
src/main/docker/     production container image
nginx/               reverse proxy configuration
ansible/             deployment inventory and playbook
docs/                requirements, design, operations, and viva material
```
