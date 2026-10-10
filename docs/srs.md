# Software Requirements Specification (SRS)

## 1. Introduction

### 1.1 Purpose
This document specifies the functional and non-functional requirements for the Automated Streetlight Fault Reporting System, a web application for citizens and municipal staff to report, track, and resolve streetlight faults.

### 1.2 Scope
The system supports three user roles (Citizen, Technician, Admin) with role-based access control, complaint management workflow, search and filtering, and a summary dashboard. The DevOps lifecycle includes CI/CD, Docker containerization, Selenium testing, and Ansible provisioning.

### 1.3 Definitions, Acronyms, and Abbreviations

| Term | Definition |
|------|------------|
| API | Application Programming Interface |
| BCrypt | Password hashing algorithm |
| CSS | Cascading Style Sheets |
| DB | Database |
| DevOps | Development and Operations combined practices |
| ENUM | Enumerated type in database |
| GUI | Graphical User Interface |
| JPA | Java Persistence API |
| JWT | JSON Web Token (not used; form-based login) |
| ORM | Object-Relational Mapping |
| PK | Primary Key |
| SLA | Service Level Agreement |
| SQL | Structured Query Language |
| UI | User Interface |
| URL | Uniform Resource Locator |
| UTF-8 | Unicode Transformation Format 8-bit |

### 1.4 References
- Spring Boot 3.x Documentation
- Spring Security 6.x Documentation
- Thymeleaf 3.x Documentation
- MySQL 8.0 Documentation
- Maven 3.9.x Documentation
- Docker Documentation
- Jenkins Documentation
- Ansible Documentation

## 2. Functional Requirements

### 2.1 User Authentication and Authorization

| Requirement ID | Requirement | Priority |
|----------------|-------------|----------|
| FR-01 | System shall authenticate users via form-based login | High |
| FR-02 | System shall use BCrypt password hashing (not plain text) | High |
| FR-03 | System shall implement three roles: CITIZEN, TECHNICIAN, ADMIN | High |
| FR-04 | System shall enforce role-based URL access (Spring Security) | High |
| FR-05 | System shall invalidate session on logout | High |
| FR-06 | System shall provide password reset (token-based, future enhancement) | Medium |
| FR-07 | Admin can create initial administrator account via bootstrap | High |

### 2.2 Complaint Management

| Requirement ID | Requirement | Priority |
|----------------|-------------|----------|
| FR-08 | Citizen shall create complaint with: streetName, ward, faultCategory, description, priority | High |
| FR-09 | Complaint must have unique complaintId (format: SL-YEAR-SEQ) | High |
| FR-10 | Complaint status defaults to REPORTED upon creation | High |
| FR-11 | Complaint associated with authenticated user (reporter) | High |
| FR-12 | Admin can assign technician to complaint | High |
| FR-13 | Technician can view assigned or ward-accessible complaints | High |
| FR-14 | Complaint fields updatable by authorized roles only | High |
| FR-15 | Admin can delete complaints; Citizen and Technician cannot | High |
| FR-16 | Complaint can be reopened if status = RESOLVED (admin only) | Medium |

### 2.3 Status Workflow

| Requirement ID | Requirement | Priority |
|----------------|-------------|----------|
| FR-17 | Controlled workflow: REPORTED → ACKNOWLEDGED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED | High |
| FR-18 | Server-enforced valid state transitions only | High |
| FR-19 | Invalid transitions rejected with error message | High |
| FR-20 | Status history recorded: previousStatus, newStatus, timestamp, user | High |
| FR-21 | High-priority and emergency complaints highlighted in UI | Medium |
| FR-22 | Resolution notes required when transitioning to RESOLVED | High |
| FR-23 | Admin can reopen RESOLVED complaints to IN_PROGRESS | Medium |

### 2.4 Search and Filtering

| Requirement ID | Requirement | Priority |
|----------------|-------------|----------|
| FR-24 | Search by complaint ID | High |
| FR-25 | Search by street name/location | High |
| FR-26 | Search by ward/area | High |
| FR-27 | Search by fault category (NOT_WORKING, FLICKERING, DAMAGED_POLE, EXPOSED_WIRING, OTHER) | High |
| FR-28 | Search by status (REPORTED, ACKNOWLEDGED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED) | Medium |
| FR-29 | Search by priority (LOW, MEDIUM, HIGH, EMERGENCY) | Medium |
| FR-30 | Search by date range (reported date) | Medium |
| FR-31 | Pagination for complaint listings and search results | High |
| FR-32 | Empty state displayed when no results found | Medium |

### 2.5 Summary Dashboard

| Requirement ID | Requirement | Priority |
|----------------|-------------|----------|
| FR-33 | Dashboard shows total complaints count | High |
| FR-34 | Dashboard shows open complaints (status != CLOSED) | High |
| FR-35 | Dashboard shows assigned complaints (assignedTo IS NOT NULL) | High |
| FR-36 | Dashboard shows in-progress complaints (status = IN_PROGRESS) | High |
| FR-37 | Dashboard shows resolved complaints (status = RESOLVED) | High |
| FR-38 | Dashboard shows high-priority + emergency complaints | High |
| FR-39 | Dashboard shows recent complaints (last 5 by reportedAt) | Medium |
| FR-40 | Dashboard shows resolution rate (RESOLVED / total * 100) | Medium |
| FR-41 | Dashboard values computed from actual database records | High |
| FR-42 | Empty state shown when no complaints exist | Medium |

### 2.6 Validation and Error Handling

| Requirement ID | Requirement | Priority |
|----------------|-------------|----------|
| FR-43 | Server-side validation on all complaint fields | High |
| FR-44 | Validation errors displayed next to form fields | High |
| FR-45 | Useful error messages (not generic) | High |
| FR-46 | CSRF protection on all form submissions | High |
| FR-47 | Global exception handler for unexpected errors | Medium |
| FR-48 | Validation on status transition (previous must match current) | High |

### 2.7 Throttling and Rate Limiting

| Requirement ID | Requirement | Priority |
|----------------|-------------|----------|
| FR-49 | Login attempt throttling (max 5 attempts per minute) | Medium |
| FR-50 | API request rate limiting per IP | Medium |

## 3. Non-Functional Requirements

### 3.1 Performance Requirements

| Requirement ID | Requirement | Acceptance Criteria |
|----------------|-------------|---------------------|
| NFR-01 | Page load time < 3 seconds for dashboard and complaint lists | Measured with Chrome DevTools; average load time under 3s on local network |
| NFR-02 | Complaint submission < 2 seconds | From form submit to database record visible |
| NFR-03 | Search response < 1 second | From search button to results displayed |
| NFR-04 | Status update < 1 second | From status change to UI refresh |

### 3.2 Security Requirements

| Requirement ID | Requirement | Acceptance Criteria |
|----------------|-------------|---------------------|
| NFR-05 | Passwords stored as BCrypt hashes | Password column never contains plain text; verification via PasswordEncoder |
| NFR-06 | Role-based access control | ADMIN endpoints inaccessible to CITIZEN/TECHNICIENT without proper role |
| NFR-07 | CSRF protection enabled for all forms | CSRF token present in forms; invalid CSRF rejects submission |
| NFR-08 | No credentials committed to Git | .gitignore excludes .env; no passwords in source code |
| NFR-09 | Secure headers (Spring Security) | X-Content-Type-Options, X-Frame-Options, Content-Security-Policy headers |

### 3.3 Availability and Reliability

| Requirement ID | Requirement | Acceptance Criteria |
|----------------|-------------|---------------------|
| NFR-10 | Application uptime > 99% during development/testing | Docker container restarts on failure; health check enables monitoring |
| NFR-11 | Docker container restarts on MySQL failure | Health check depends on database; container restart policy: always |
| NFR-12 | Graceful shutdown on SIGTERM | Spring Boot handles graceful shutdown |

### 3.4 Maintainability Requirements

| Requirement ID | Requirement | Acceptance Criteria |
|----------------|-------------|---------------------|
| NFR-13 | Code follows Java conventions and Spring Best Practices | Code review passes; static analysis (checkstyle) no critical issues |
| NFR-14 | Configuration via application.yml / environment variables | Properties not hardcoded; .env.example with placeholders |
| NFR-15 | Clear layering: Controller → Service → Repository → Entity | Each layer has single responsibility; no layer skipping |
| NFR-16 | Documentation updated with each milestone | All 18 required documents exist and contain relevant content |

### 3.5 Portability Requirements

| Requirement ID | Requirement | Acceptance Criteria |
|----------------|-------------|---------------------|
| NFR-17 | Application runs on Windows, macOS, Linux | Docker Compose works across platforms; Maven Wrapper platform-agnostic |
| NFR-18 | Database independent (MySQL 8 primary, H2 for tests) | Tests run with H2; production uses MySQL 8; entity annotations dialect-compatible |
| NFR-19 | Maven Wrapper (mvnw) platform-agnostic | mvnw.cmd for Windows, ./mvnw for Linux/macOS |

### 3.6 Technology Stack Compliance

| Requirement ID | Requirement | Compliance |
|----------------|-------------|------------|
| NFR-20 | Java 21 as target version | Project compiled with Java 21 compiler settings |
| NFR-21 | Spring Boot 3.x with Spring MVC | Using spring-boot-starter-web, spring-boot-starter-thymeleaf |
| NFR-22 | Maven as build tool | pom.xml with maven-wrapper configuration |
| NFR-23 | MySQL 8 as primary database | Entity configuration, Flyway migration script |
| NFR-24 | H2 database for local unit tests | Test configuration with spring-boot-starter-h2 |
| NFR-25 | Thymeleaf as server-side template engine | All views via Thymeleaf HTML templates |
| NFR-26 | Bootstrap 5 for styling | HTML templates use Bootstrap 5 CSS classes |
| NFR-27 | Selenium for automated testing | WebDriver tests with ChromeDriver/Jupiter |
| NFR-28 | Jenkins Pipeline (Jenkinsfile) for CI/CD | 12 stages as specified |
| NFR-29 | Docker and Docker Compose for containerization | Dockerfile, docker-compose.yml |
| NFR-30 | Nginx as reverse proxy | Configuration documented; optional for local dev |
| NFR-31 | Ansible for configuration management | Playbook with roles/tasks for deployment |
| NFR-32 | Docker Hub for image registry | Image publishing workflow documented |
| NFR-33 | Git and GitHub for version control | Branching strategy; conventional commits |
| NFR-34 | Mermaid diagrams in documentation | Use-case, architecture, ERD, DevOps lifecycle diagrams |
| NFR-35 | 15 assignment milestones with documentation | All 15 milestones addressed in docs |

## 4. Interface Requirements

### 4.1 User Interfaces

| Interface | Description |
|-----------|-------------|
| Login Page | Form with username/password; error message for invalid credentials |
| Dashboard | Cards with counts; recent complaints list; metrics summary |
| Complaint List | Table with pagination; search/filter controls; action buttons role-dependent |
| Complaint Detail | Complaint info; status history timeline; resolution notes; role-specific actions |
| Complaint Form | Fields: streetlightId, streetName, ward, faultCategory, description, priority; submit/cancel |
| Admin/Technician Views | Additional buttons/links for manage/assign/reopen actions |

### 4.2 API Interfaces

| Integration | Description |
|-------------|-------------|
| Spring MVC Controllers | REST endpoints for complaint CRUD, search, status updates |
| Thymeleaf Templates | Server-rendered HTML; form binding; `${}` and `*{}` expressions |
| Spring Data JPA | Repository methods; query by method name; pagination |
| MySQL Driver | JDBC connection via spring.datasource.url/username/password |
| Docker Compose | Service orchestration: app, MySQL, Nginx |
| Jenkins Pipeline | Stage-by-stage build, test, deploy orchestration |
| Ansible Playbook | Task execution on target host; package installation; service management |
| Selenium WebDriver | Browser automation; element interaction; screenshot on failure |

### 4.3 Database Interfaces

| Interface | Description |
|-----------|-------------|
| JDBC / Spring Data JPA | Database operations via repository interfaces |
| Flyway Migration | Versioned schema migration (V1__initial_schema.sql) |
| Entity Relationships | JPA annotations: @Entity, @Table, @Id, @Column, @ManyToOne, @OneToMany |
| Indexes | Performance indexes on: status, priority, ward, complaintId |
| Triggers | Auto-timestamp: created_at, updated_at on Complaint |

### 4.4 External Interfaces

| Interface | Technology | Purpose |
|-----------|------------|---------|
| Web Browser | Chrome, Edge, Firefox | User access to web application |
| Docker Engine | Docker Desktop | Container orchestration |
| Docker Compose | v2.40+ | Multi-container application |
| Jenkins | LTS LTS | CI/CD pipeline execution |
| Maven | 3.9.x+ | Build and test automation |
| Git | 2.+ | Version control |
| GitHub | Hosted repository | Remote source code storage |
| Docker Hub | Cloud registry | Docker image storage and distribution |
| Ansible | 9.x+ | Infrastructure as code provisioning |
| Selenium Grid (optional) | Browser automation | Distributed test execution |

## 5. External Non-Functional Requirements

### 5.1 Deployment Environment
- Docker Desktop 4.+ or equivalent
- Docker Compose v2.+
- MySQL 8.0 or compatible
- OpenJDK 21
- Maven 3.9.x
- Git 2.+
- Jenkins LTS (or local pipeline execution)
- Ansible 9.x+ (for provisioning)
- Nginx (optional, reverse proxy)

### 5.2 Development Environment
- Java 21 JDK
- Maven 3.9.x (via mvnw wrapper)
- Git for version control
- IDE (IntelliJ IDEA, Eclipse, or VS Code)
- Chrome browser (for Selenium tests)
- Docker Desktop running
- Local network access (8080 or documented URL)

### 5.3 Operational Environment
- Production-like deployment via Docker Compose
- Health check endpoints for monitoring
- Rollback procedure for deployment failures
- Environment-specific configuration via .env variables
- Logs archived from Jenkins pipeline runs

## 6. Acceptance Test Criteria

The system satisfies all functional and non-functional requirements when:

- [ ] All 15 user stories (US-1 through US-15) pass acceptance testing
- [ ] All 5 Selenium tests pass (login, submit, search, status update, unauthorized)
- [ ] Maven build: `./mvnw clean test` completes without errors
- [ ] Docker image builds: `docker build -t <repo>:<tag>` succeeds
- [ ] Docker Compose: `docker compose up -d` brings up all services
- [ ] Application accessible at documented URL
- [ ] Dashboard values match database records (verified via MySQL client)
- [ ] Status workflow transitions enforced server-side
- [ ] Role-based access: no role can access another role's restricted endpoints
- [ ] Ansible playbook runs idempotently (second run: no unnecessary changes)
- [ ] Jenkins pipeline completes all 12 stages on successful build
- [ ] Failed tests block Docker image build and deployment
- [ ] Version tag `v1.0.0` created and annotated
- [ ] All 18 required documentation files created and populated
- [ ] Mermaid diagrams generated for: use-case, architecture, ER diagram, DevOps lifecycle
- [ ] Git history shows meaningful commits across main/develop/feature branches
- [ ] `.env.example` contains placeholder values, not real secrets
- [ ] `.gitignore` excludes: target/, .env, .m2/, Docker credentials

---