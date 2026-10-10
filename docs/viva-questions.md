# Viva Questions and Answers

## 1. Architecture and Design

### Q1: Explain the overall architecture of your application.
**A:** The application follows a layered architecture with clear separation of concerns:
- **Presentation Layer**: Thymeleaf HTML templates with Bootstrap 5 for responsive design
- **Controller Layer**: Spring MVC handlers that process HTTP requests, bind request parameters, and prepare models for templates
- **Service Layer**: Business logic for complaint management, status workflow enforcement, validation, and history recording
- **Repository Layer**: Spring Data JPA with Hibernate ORM for database operations; custom query methods for search/filter/pagination
- **Entity Layer**: JPA entities for User, Complaint, and StatusHistory with proper relationships and constraints
- **Database Layer**: MySQL 8 as the primary database with Flyway for versioned schema migrations

The layers communicate through well-defined interfaces: controllers call service methods, services use repository methods, and repositories execute JPQL/native SQL queries. This layered approach makes the code maintainable and testable.

### Q2: How do you handle role-based authorization in your application?
**A:** I use Spring Security 6.x with form-based login and BCrypt password hashing. Three roles are defined: CITIZEN, TECHNICIAN, and ADMIN. Authorization is enforced at two levels:

1. **Backend (primary)**: `@PreAuthorize` annotations on controller methods, e.g., `@PreAuthorize("hasRole('ADMIN')")` on assignment endpoints. This blocks unauthorized HTTP requests regardless of frontend UI.

2. **Frontend (user experience)**: Thymeleaf `sec:authorize` tags conditionally show/hide buttons and links based on the user's role, e.g., `<th:block th:secures="hasRole('ADMIN')">...</th:block>`.

I emphasize that server-side authorization is the primary defense; hiding buttons in the frontend alone is insufficient security. All sensitive endpoints have `@PreAuthorize` checks, and role-based access is tested via Selenium tests for unauthorized access restriction.

### Q3: Explain the status workflow and how you enforce valid transitions.
**A:** The controlled workflow is: REPORTED → ACKNOWLEDGED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED. This is enforced server-side in the `ComplaintService`:

- Each status transition is validated against a permitted matrix. For example, IN_PROGRESS → RESOLVED requires resolution notes; RESOLVED → CLOSED is admin-only; IN_PROGRESS → REPORTED is blocked.
- When a valid transition occurs, a new entry is created in the `status_history` table recording: previous status, new status, timestamp (auto-generated), and user who made the change.
- Invalid transitions are rejected with a descriptive error message: "Invalid status transition from X to Y."
- The workflow is designed to prevent claiming a complaint has been repaired unless an authorized user records the resolution with notes.

### Q4: How does the dashboard compute its values? Are they hardcoded?
**A:** No, all dashboard values are computed from actual database records at runtime. The dashboard endpoint (`GET /dashboard`) queries the database via repository methods:

- **Total complaints**: `SELECT COUNT(*) FROM complaints`
- **Open complaints**: `SELECT COUNT(*) FROM complaints WHERE status != 'CLOSED'`
- **Assigned complaints**: `SELECT COUNT(*) FROM complaints WHERE assigned_to IS NOT NULL`
- **In-progress**: `SELECT COUNT(*) FROM complaints WHERE status = 'IN_PROGRESS'`
- **Resolved**: `SELECT COUNT(*) FROM complaints WHERE status = 'RESOLVED'`
- **High-priority + emergency**: `SELECT COUNT(*) FROM complaints WHERE priority IN ('HIGH', 'EMERGENCY')`
- **Recent complaints**: `SELECT * FROM complaints ORDER BY created_at DESC LIMIT 5`
- **Resolution rate**: `SELECT COUNT(CASE WHEN status='RESOLVED' THEN 1 END)*100.0/COUNT(*) FROM complaints`

If no complaints exist, empty states display "0" or appropriate messages rather than hardcoded values.

### Q5: Explain the entity relationships in your database design.
**A:** The key entities and relationships are:

- **User (1) → (N) Complaint**: One user can submit many complaints; each complaint belongs to one user (reporter). Also: User can be assigned to many complaints as technician (assignedTo is nullable FK).
- **Complaint (1) → (N) StatusHistory**: One complaint has many status history entries; each history entry belongs to one complaint. On complaint deletion, status history cascades (ON DELETE CASCADE).
- **User (1) → (1) Role**: One user has one role (CITIZEN, TECHNICIAN, ADMIN). Role determines authorization level.

The ER diagram uses foreign keys with constraint enforcement: `ON DELETE RESTRICT` for reporter (can't delete user with complaints), `ON DELETE SET NULL` for assigned_to (technician departure), `ON DELETE CASCADE` for statusHistory (complaint deletion removes history).

Indexes exist on status, priority, ward, complaintId, reporter_id, and assigned_to for query performance, especially for dashboard and search operations.

## 2. Security

### Q6: How are passwords stored and verified?
**A:** Passwords are stored as BCrypt hashes using Spring Security's `BCryptPasswordEncoder` with strength factor 10. The plain text password is never stored in the database. During login, the entered plain text password is verified against the stored hash using `PasswordEncoder.matches()`. This ensures that even database administrators cannot reverse-engineer original passwords.

### Q7: How do you prevent unauthorized access to admin/technician endpoints?
**A:** Two-layer defense:

1. **Spring Security configuration**: Form login with `HttpSecurity` configuration; `authorizeHttpRequests()` specifies which URLs require which roles. `@PreAuthorize` annotations on controller methods provide method-level security.

2. **Thymeleaf `sec:authorize`**: UI elements (buttons, links) are conditionally rendered based on the authenticated user's role. However, I emphasize that this is supplementary; the primary defense is the backend `@PreAuthorize` checks.

3. **Selenium testing**: I have automated tests that verify unauthorized users (Citizen role) cannot access technician/admin-only pages; attempting to do so results in 403 Forbidden or redirect to login.

### Q8: What is CSRF protection and how is it implemented?
**A:** CSRF (Cross-Site Request Forgery) protection is enabled by default in Spring Security for all form submissions. Each form includes a CSRF token field (`<input type="hidden" th:field="*${_csrf.token}">`). Submissions without a valid CSRF token are rejected with a 400 Bad Request error. This prevents malicious sites from submitting forms on behalf of authenticated users.

### Q9: How do you handle password strength and validation?
**A:** During registration, I validate that the password meets minimum length requirements (6+ characters). The actual password hashing uses BCrypt with strength 10. I also validate password confirmation (must match). The encoder's strength parameter controls the computational cost of hashing; strength 10 provides adequate security for a college project.

## 3. Database and Flyway

### Q10: Explain your database migration strategy.
**A:** I use Flyway for versioned schema migrations. Migration files are stored in `src/main/resources/db/migration/` named `V1__initial_schema.sql`, `V2__add_...sql`, etc. On first deployment, Flyway baseline creates the schema version table, then applies migrations in order. On subsequent runs, only new (unapplied) migrations are executed. `baseline-on-migrate: true` in `application.yml` handles existing databases gracefully. `ddl-auto=none` in Hibernate prevents automatic schema generation; the database schema evolves solely through Flyway migrations.

### Q11: What if the database already has data? How does Flyway handle it?
**A:** `baseline-on-migrate: true` means Flyway will baseline the existing database if it has no prior Flyway history, then apply new migrations from the baseline version forward. `baseline-version: "0"` initializes the schema version to 0 for fresh setups. For production upgrades, new migration files (V2__, V3__, etc.) are added, and Flyway applies only those new migrations without affecting existing data.

### Q12: The application uses MySQL 8 and H2 for tests. How are they different and how do you ensure compatibility?
**A:** The production database is MySQL 8, configured via `spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.MySQL8Dialect`. For local unit testing, `spring.profiles.active=test` switches to H2 in-memory database (`spring.datasource.url=jdbc:h2:mem:testdb`). Entity annotations are written to be compatible with both; the dialect difference is the main variation. Tests run with H2; production uses MySQL. The Flyway migration script (`V1__initial_schema.sql`) is written for MySQL 8 syntax and applied to both during their respective setup phases.

## 4. CI/CD and DevOps

### Q13: Walk me through your Jenkins pipeline stages.
**A:** The Jenkinsfile has 12 stages:

1. **Checkout**: Retrieve source code from GitHub via webhook or polling
2. **Environment and Java version verification**: `java -version`, `./mvnw --version`, check `MAVEN_OPTS`
3. **Maven clean and compile**: `./mvnw clean compile`; must succeed to proceed
4. **Unit tests**: `./mvnw test`; JUnit 5 + Mockito tests; failure stops pipeline
5. **Integration tests**: `./mvnw verify -Dspring.profiles.active=test`; Spring Boot Test with H2; failure stops pipeline
6. **Package the application**: `./mvnw package`; produces JAR in `target/`
7. **Archive the JAR artifact**: `archiveArtifacts` in Jenkins; JAR available in build artifacts section
8. **Build a Docker image**: `docker build -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}`; tag includes build number; push to Docker Hub using Jenkins credentials
9. **Run Selenium tests against deployed app**: Deploy test app via Docker Compose; run 5 WebDriver user journey tests; failure stops deployment
10. **Publish test reports**: `junit` publisher for JUnit XML; archive Selenium screenshots/failure reports
11. **Deploy only if quality gates pass**: `when` expression checks `currentBuild.result == 'SUCCESS'`; only runs if all prior stages passed (especially stage 9 Selenium)
12. **Deployment health check**: `curl` to `/actuator/health`; wait up to 150s (30 retries × 5s); if fails, trigger rollback to previous Docker image

If any stage fails, the pipeline stops, and deployment to production is blocked. Quality gates ensure no broken code or failing tests reach production.

### Q14: How do failed tests block deployment?
**A:** The Jenkinsfile uses `when` expressions and stage ordering to enforce quality gates:

- Stage 4 (unit tests): if any test fails, `currentBuild.result = 'FAILURE'`; stage 5 never starts (depends on previous)
- Stage 5 (integration tests): same; failure blocks subsequent stages
- Stage 9 (Selenium tests): if any of the 5 user journey tests fails, `currentBuild.result = 'FAILURE'`; stage 11 (deploy) has `when { expression { return currentBuild.result == 'SUCCESS' } }` which skips deployment
- Stage 11 (deploy): only runs if all prior gates passed; if Selenium or earlier tests failed, deployment is skipped entirely
- Stage 12 (health check): if the deployed app fails health check, rollback procedure initiates (pull previous image tag, `docker compose up -d`)

The pipeline philosophy is: **failed tests = block deployment**. This ensures that only code with passing tests reaches production.

### Q15: How is the pipeline triggered on Git commits?
**A:** Two mechanisms:

1. **GitHub webhook (preferred)**: GitHub repo Settings → Webhooks → Add webhook; Payload URL: `http://<JENKINS_URL>:8080/github-webhook/`; Content type: `application/json`; Events: "Just push events". On every push to any branch, Jenkins triggers the pipeline immediately.

2. **Polling (alternative)**: In Jenkins job configuration → "Build triggers" → "Poll SCM"; schedule: `H/5 * * * *` (every 5 minutes). Jenkins checks GitHub for new commits every 5 minutes.

The webhook is faster (near-instant), while polling is more reliable if webhook infrastructure fails. Both are documented; the project typically uses the webhook when Jenkins is available.

## 5. Docker and Containerization

### Q16: Explain your Dockerfile and Docker Compose configuration.
**A:** 

**Dockerfile** (production-oriented):
- Base image: `openjdk:21-jdk-slim` (non-root user where possible)
- Copy JAR from build stage (multistage build recommended)
- Set working directory; expose port 8080
- Set entrypoint: `java -jar app.jar`
- ENV variables for database config (not hardcoded)
- HEALTHCHECK instruction: `CMD curl -f http://localhost:8080/actuator/health || exit 1`
- USER instruction for non-root execution (e.g., USER 1000:1000)

**Docker Compose** (development + production):
- **app service**: builds from Dockerfile; depends on MySQL; health check on `/actuator/health`; restart: unless-stopped; environment variables for DB connection
- **mysql service**: `mysql:8.0` image; persistent volume (`mysql_data`); environment for root/user/password; health check with `mysqladmin ping`; ports: 3306
- **nginx service** (optional): `nginx:alpine`; ports 80/443; reverse proxy to app:8080; SSL termination if certs available; depends on app and MySQL being healthy

Service dependencies and readiness/health checks ensure MySQL is up before the app starts; the app is up before Nginx attempts to proxy.

### Q17: How do you handle rollback in Docker?
**A:** Rollback procedure:

1. Identify the previous known-good image tag (from Docker history or Jenkins environment variable `PREVIOUS_IMAGE_TAG`)
2. `docker pull <registry>/<image>:<previous_tag>`
3. `docker tag <previous_tag> <registry>/<image>:latest` (or desired tag)
4. `docker compose down` (stop current containers)
5. `docker compose up -d` (restart with previous image)
6. Verify: `docker compose ps`, `curl http://localhost:8080/actuator/health`
7. If rollback also fails: manual intervention, check Docker Hub, escalate

In Jenkins, Stage 12 health check triggers automated rollback if the new deployment's health check fails after retries.

### Q18: How do you configure environment variables in Docker?
**A:** Two approaches:

1. **`.env` file**: Local development; key=value pairs loaded by Docker Compose; `.env` file with actual values (excluded from Git via `.gitignore`); `.env.example` with placeholder values committed to repo

2. **Jenkins credentials**: In Jenkinsfile, `withCredentials([usernamePassword(credentialsId: 'docker-hub-credentials', passwordVar: 'DOCKER_PASS', usernameVar: 'DOCKER_USER')])`; used for `docker login` and `docker push`; registry URL, username, password stored securely in Jenkins, not in source code

3. **Docker Compose `env_file`**: Points to `.env` file; or individual `environment:` keys in `docker-compose.yml`

The principle is: never embed secrets in Docker images; always use environment variables at runtime.

## 6. Ansible and Provisioning

### Q19: Explain the Ansible playbook structure.
**A:** The Ansible setup includes:

- **inventory.yml**: Target host(s) IP/credentials; `[streetlight-app]` group
- **vars.yml**: Placeholder variables (NOT real passwords); `db_password: CHANGE_ME_PRODUCTION`; overrides at runtime via `--extra-vars`
- **site.yml**: Main playbook that orchestrates roles in order: `system` → `docker` → `application` → `health`
- **roles/system**: Package installation (curl, wget, apt-utils); create deployment directories (`/opt/streetlight`); disable swap (required for Docker); create users/directories
- **roles/docker**: Install Docker Engine + Compose; add user to docker group; start/enable Docker service
- **roles/application**: Copy Docker Compose file; copy `.env` and `application.yml` configs; pull Docker image from registry; start `docker compose up -d`
- **roles/health**: Check `/actuator/health` endpoint; verify application responsiveness; idempotency verification; record deployment timestamp

### Q20: How do you demonstrate idempotency with Ansible?
**A:** Run the playbook twice and verify:

```
# First run - makes changes to reach desired state
ansible-playbook -i inventory.yml site.yml

# Expected: changed=12 (or some number, depending on initial state)

# Second run - should produce no unnecessary changes
ansible-playbook -i inventory.yml site.yml

# Expected: changed=0  (idempotency confirmed)
```

The `PLAY RECAP` shows `target-host : ok=5 changed=0 unreachable=0 failed=0 skipped=0 rescued=0`. This confirms all tasks are idempotent (using idempotent modules: `apt`, `file`, `service`, `docker_image`, `docker_compose`).

### Q21: What if the target server doesn't have SSH access? How do you handle Ansible provisioning?
**A:** The project documents that Ansible assumes SSH access to the target Linux server. If SSH access is unavailable, the provisioning steps are documented as manual procedures:

1. Install Java 21 OpenJDK
2. Install Docker Desktop / Engine + Compose
3. Install Nginx (optional, for reverse proxy)
4. Create deployment directory (`/opt/streetlight`)
5. Copy application `.env` and `application.yml`
6. Pull Docker image from Docker Hub
7. `docker compose up -d` to start services
8. Verify: `docker ps`, `curl localhost:8080/actuator/health`

The Ansible playbook is the "ideal" automated path; manual steps are documented as fallback for environments without SSH. The playbook can also be run with `--ask-pass` for password-based SSH, or with SSH key-based authentication (`authorized_keys` on target, `~/.ssh/id_rsa` on control machine).

### Q22: What are the most common Ansible failures and how do you troubleshoot them?
**A:** Common issues:

| Problem | Cause | Solution |
|---------|-------|----------|
| `Unreachable host` | Wrong IP; SSH not running; firewall | `ping <ip>`; `service ssh start`; `ufw allow 22`; verify `inventory.yml` |
| `Permission denied (publickey,password)` | SSH key not authorized; wrong credentials | `ssh user@host` manually; generate new key; `ssh-copy-id user@host`; use `--ask-pass` |
| `Package install failed` | Repo not accessible; wrong package name | `apt-get update` first; `apt-cache search`; verify package names |
| `Docker not found` | Docker not installed | Run `ansible-playbook -i inventory.yml site.yml --tags "docker"` to install Docker first |
| `Swap not disabled` | Docker fails to start if swap enabled | `swapoff -a`; edit `/etc/fstab` to remove swap line; re-run playbook |
| `Variable substitution failed` | Jinja2 template syntax error | Check `.j2` files; ensure `{{ variable }}`; verify variables defined in `vars.yml` or `--extra-vars` |
| `Idempotency not working` | Non-idempotent tasks | Use idempotent modules; add `changed_when: false`; use `--diff` to see what changed |

## 7. Testing

### Q23: What testing frameworks do you use and how do they integrate?
**A:** 

- **JUnit 5**: Unit tests; `@Test`, `@BeforeEach`, `@AfterEach`, `@ExtendWith(SpringExtension.class)`; runs via `./mvnw test`
- **Mockito**: Mocking repository/service dependencies in unit tests; `@Mock`, `@Spy`, `@InjectMocks`; runs with JUnit 5 via `@ExtendWith(MockitoExtension.class)`
- **Spring Boot Test**: Integration tests with `@SpringBootTest`, `@AutoConfigureMockMvc`, `@ActiveProfiles("test")` (H2 in-memory); runs via `./mvnw test -Dspring.profiles.active=test`
- **Selenium WebDriver**: End-to-end UI tests; 5 user journey tests; ChromeDriver; runs via `./mvnw test -Dtest="*SeleniumTest"` or surefire plugin configuration

All test levels integrate through Maven Surefire plugin in `pom.xml`; the Jenkins pipeline runs all levels as quality gates.

### Q24: How do your Selenium tests work and what do they cover?
**A:** The 5 Selenium user journey tests are:

1. **TG-1: User login and logout** - Navigate to `/login`, enter valid credentials, submit; verify dashboard access; click logout; verify redirected to login page. Includes positive (successful login) and negative (invalid credentials) cases.

2. **TG-2: Successful complaint submission** - Login as citizen; navigate to `/complaints/new`; fill form with valid data (streetName, ward, faultCategory, description, priority); submit; verify success message; verify complaint appears in user's list with status REPORTED and unique complaintId.

3. **TG-3: Search for existing complaint** - Login; navigate to search page; enter search criteria (e.g., ward, faultCategory); verify results filter correctly; verify pagination; verify empty state when no matches; combined search (multiple criteria).

4. **TG-4: Authorized status update** - Login as technician; open complaint detail; select valid next status from workflow matrix; if transitioning to RESOLVED, enter resolution notes; click "Update Status"; verify status changes; verify status history entry created with previousStatus, newStatus, timestamp, changedBy; invalid transition blocked with error.

5. **TG-5: Unauthorized access restriction** - Login as citizen; attempt to access admin-only URL (e.g., `/complaints/{id}/assign`); verify 403 Forbidden or redirect to access-denied page; verify citizen cannot assign technicians or reopen resolved complaints.

Each test uses explicit `WebDriverWait` with `ExpectedConditions` (no arbitrary `Thread.sleep()`). Failure screenshots are captured via `TakesScreenshot` for debugging. Tests run with Chrome in headless mode for CI environments.

### Q25: How do you ensure tests are reliable and not flaky?
**A:** Several practices:

- **Explicit waits**: `WebDriverWait(driver, Duration.ofSeconds(10))` with `ExpectedConditions.presenceOfElementLocated()` rather than `Thread.sleep()`
- **Test isolation**: Each test sets up its own data in `@BeforeEach`; H2 database fresh per test class; no reliance on test execution order
- **Deterministic data**: Controlled test usernames/passwords; known complaint states; no random data
- **Headless mode for CI**: Chrome with `--headless --no-sandbox --disable-dev-shm-usage`; same behavior as headed mode for critical assertions
- **Screenshots on failure**: `driver.getScreenshotAs(OutputType.FILE)` saves PNG; included in test reports for debugging
- **Meaningful assertions**: `assertTrue(isElementPresent(...)), "Expected login form to be visible")` with descriptive messages; not just `assertTrue(true)`
- **Local before CI**: Tests run locally (`./mvnw test`) before pushing; ensure they pass before committing

### Q26: What test coverage do you have and how do you measure it?
**A:** 

- **Unit tests**: Cover service layer and repository methods; key business logic (status workflow validation, complaint creation with validation, security checks); typically 60-80% of application code branches
- **Integration tests**: Cover REST endpoint behavior, database operations with H2, security context; typically 15-20% of code paths
- **Selenium tests**: Cover 5 critical user journeys from UI perspective; functional coverage of all role-based workflows

I use JaCoCo Maven plugin to generate test coverage reports (`./mvnw jacoco:report`); reports appear in `target/site/jacoco/`. For the college project, the emphasis is on having tests that cover the critical paths and quality gates in CI rather than achieving a specific percentage number.

## 8. Final Project Status

### Q27: What is the current project status?
**A:** The Automated Streetlight Fault Reporting System is at MVP (Minimum Viable Product) stage with all 15 assignment milestones addressed. The system includes:

- Working web application with Citizen/Technician/Admin roles
- Complete complaint CRUD with validation
- Controlled status workflow with audit trail
- Summary dashboard with real database metrics
- Role-based access control (server-side enforced)
- Docker containerization with MySQL + Nginx
- Jenkins CI/CD pipeline (12 stages) with Selenium quality gates
- Ansible provisioning playbook with idempotency
- 18 required documentation files with Mermaid diagrams
- Git branching strategy with conventional commits
- Version tag `v1.0.0` for release-ready baseline

All documentation, code, testing, and DevOps infrastructure is in place and functional.

### Q28: What are the known limitations?
**A:** Key limitations (documented in `docs/limitations-and-future-scope.md`):

1. No email notification system
2. No file attachments (photos)
3. No date-range search
4. H2 database for tests only (MySQL for production)
5. No Kubernetes/Docker Swarm orchestration
6. HTTP only (no HTTPS by default)
7. Session-based auth (no JWT)
8. No API versioning
9. Dashboard metrics only (no trends/graphs)
10. English-only UI

These are documented trade-offs for the college project scope; many can be added in future enhancements.

### Q29: What future enhancements are planned?
**A:** Prioritized enhancements:

- **High value**: Email notifications, file attachments, advanced search with date range
- **Medium value**: Role sub-permissions, resolution rate trends, user profile management
- **Lower value**: Dashboard analytics exports, i18n, GIS/map integration
- **Architecture**: Kubernetes migration, microservices breakdown, cloud deployment

The modular architecture accommodates these additions without refactoring core functionality.

### Q30: "If you were to start this project again, what would you do differently?"
**A:** Key improvements for a redo:

1. **Start with Docker Compose** earlier (not after full local development); containerize from day one
2. **Add Selenium tests during feature development**, not as a final phase; tests guide design
3. **Use Ansible from the first deployment**, not as an afterthought; infrastructure as code from start
4. **Document API endpoints earlier** (OpenAPI/Swagger); would have helped frontend-backend coordination
5. **Plan the database schema more carefully** with indexing strategy from the start; avoided some performance optimizations later
6. **Write more unit tests before integration tests**; test-driven development approach for core service logic
7. **Use a consistent coding convention tool** (checkstyle, spotless) from project beginning; avoided retroactive formatting
8. **Plan the `.env`/secrets strategy from the beginning**; avoided moving credentials out of source code later

Overall, the project successfully delivers all 15 assignment milestones and provides a functional, tested, containerized application suitable for college viva presentation and demonstration.