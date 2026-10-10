# Product Backlog

## User Stories (Prioritized)

| ID | User Story | Role | Priority | Acceptance Criteria |
|----|------------|------|----------|---------------------|
| US-1 | As a citizen, I want to register an account so that I can submit complaints | Citizen | High | Registration form with username/password; account created with CITIZEN role; auto-login after registration |
| US-2 | As a citizen, I want to log in so that I can access the complaint system | Citizen | High | Form login with BCrypt-encoded password; session management; logout invalidates session |
| US-3 | As a citizen, I want to submit a new streetlight fault complaint so that it can be tracked | Citizen | High | Form requires: streetName, ward, faultCategory, description, priority; validation errors for missing fields; complaint assigned unique ID; status starts as REPORTED |
| US-4 | As a citizen, I want to view my submitted complaints so that I can track their status | Citizen | High | Complaints list filtered by authenticated user; pagination; can click to view detail |
| US-5 | As a technician, I want to view assigned complaints so that I can repair them | Technician | High | List shows complaints where assignedTo = current user; also shows ward-accessible complaints |
| US-6 | As a technician, I want to update complaint status so that repair progress is recorded | Technician | High | Status changes follow valid workflow: REPORTED→ACKNOWLEDGED→ASSIGNED→IN_PROGRESS→RESOLVED→CLOSED; invalid transitions blocked; resolution notes required for RESOLVED |
| US-7 | As an admin, I want to manage all complaints so that I can oversee the workflow | Admin | High | Can view all complaints regardless of assignment; can assign technicians; can change status; can reopen resolved complaints |
| US-8 | As an admin, I want to view the summary dashboard so that I can see key metrics | Admin | High | Cards show: total complaints, open complaints, assigned complaints, in-progress, resolved, high-priority, emergency, recent complaints; values from database |
| US-9 | As a user, I want to search for complaints by various criteria so that I can find specific reports | All | Medium | Search by: complaint ID, street name, ward, fault category, status, priority; results pagination; clear empty state |
| US-10 | As a system, I want valid status workflow transitions enforced so that data integrity is maintained | System | High | Only allowed transitions accepted; previous status, new status, timestamp, and user recorded in status_history table |

## Technical Stories

| ID | Technical Story | Description | Priority |
|----|-----------------|-------------|----------|
| TS-1 | Set up Spring Boot 3.x project with Maven Wrapper | Create project structure, pom.xml, Java 21 compatibility | High |
| TS-2 | Configure Spring Security with role-based authorization | Form login, BCrypt password encoding, URL access by role | High |
| TS-3 | Create User entity with Role enum and BCrypt password hashing | JPA entity, constraints, relationships | High |
| TS-4 | Create Complaint entity with all fields, ENUM types, and relationships | JPA entity, constraints, indexes, timestamps | High |
| TS-5 | Create StatusHistory entity for audit trail | JPA entity, foreign key to Complaint, timestamp auto-generation | High |
| TS-6 | Create Complaint repository with JPA + query methods | Spring Data JPA, search/filter methods, pagination | High |
| TS-7 | Implement complaint service with business logic | Workflow validation, status transition enforcement, history recording | High |
| TS-8 | Implement REST controllers and Thymeleaf templates | CRUD operations, search forms, dashboard, role-specific views | High |
| TS-9 | Implement role-based access control in Thymeleaf | `sec:authorize` tags, hide/show buttons based on role | Medium |
| TS-10 | Create global exception handler | Useful error messages, validation errors, not found pages | Medium |
| TS-11 | Configure MySQL 8 and H2 for testing | Database setup, Flyway migration, test configuration | High |
| TS-12 | Implement Selenium tests for 5 user journeys | Login, submit complaint, search, status update, unauthorized restriction | High |
| TS-13 | Create Dockerfile and Docker Compose configuration | Application service, MySQL service, Nginx reverse proxy, health checks | High |
| TS-14 | Create Jenkinsfile pipeline with 12 stages | Checkout, compile, test, package, Docker, health check, deploy | High |
| TS-15 | Create Ansible playbook for provisioning | Inventory, variables, packages, deployment, health check, idempotency | High |

## Sprint Planning (15-Task Plan)

### Sprint 1 — Foundation (Tasks 1-5)
- TS-1: Project setup with Maven Wrapper
- TS-2: Spring Security configuration
- TS-3: User entity with Role and BCrypt
- TS-4: Complaint entity with all fields
- TS-5: StatusHistory entity for audit trail

### Sprint 2 — Core CRUD (Tasks 6-9)
- TS-6: Complaint repository with search/filter
- TS-7: Complaint service with workflow logic
- TS-8: Controllers and Thymeleaf templates
- TS-9: Role-based access in templates

### Sprint 3 — Validation and Polish (Tasks 10-11)
- TS-10: Global exception handler
- TS-11: Database configuration (MySQL + H2)

### Sprint 4 — Testing and DevOps (Tasks 12-15)
- TS-12: Selenium test design and execution
- TS-13: Docker and Docker Compose setup
- TS-14: Jenkins Pipeline configuration
- TS-15: Ansible provisioning playbook

## Acceptance Criteria by Story

### US-1 (Registration)
- [ ] POST `/register` creates user with CITIZEN role by default
- [ ] Password is BCrypt-hashed before storage
- [ ] Username is unique (constraint + error message)
- [ ] Auto-login after registration
- [ ] Redirect to dashboard upon successful registration

### US-3 (Complaint Submission)
- [ ] GET `/complaints/new` shows form with all fields
- [ ] POST `/complaints` validates: streetName, ward, faultCategory, description, priority required
- [ ] Complaint gets unique complaintId (format: SL-YEAR-XXX)
- [ ] Status defaults to REPORTED
- [ ] Complaint associated with authenticated user (reporter)
- [ ] Redirect to complaint detail page after submit
- [ ] Validation errors displayed for missing/invalid fields

### US-6 (Status Update)
- [ ] Technician can access status update form
- [ ] Only valid next states accepted (enforced server-side)
- [ ] Status history entry created with: previousStatus, newStatus, changedAt, changedBy
- [ ] IN_PROGRESS → RESOLVED requires resolution notes
- [ ] RESOLVED → CLOSED only by admin or after notes recorded
- [ ] Complaint detail page refreshes with new status
- [ ] Invalid transition shows error: "Invalid status transition from X to Y"

### US-8 (Dashboard)
- [ ] Total complaints card shows COUNT(*) from complaints table
- [ ] Open complaints = status != CLOSED
- [ ] Assigned complaints = assignedTo IS NOT NULL
- [ ] In-progress = status = IN_PROGRESS
- [ ] Resolved complaints = status = RESOLVED
- [ ] High-priority = priority = HIGH OR EMERGENCY
- [ ] Recent complaints = last 5 by reportedAt
- [ ] Resolution rate = RESOLVED / total (percentage)
- [ ] Empty state shown when no complaints exist

### US-9 (Search)
- [ ] Search by complaint ID returns specific complaint
- [ ] Search by ward filters complaints
- [ ] Search by fault category filters results
- [ ] Search by status filters results
- [ ] Search by priority filters results
- [ ] Combined search (multiple criteria) works
- [ ] Pagination on search results
- [ ] Empty state when no matches found

### TS-12 (Selenium Tests)
- [ ] Test 1: User login and logout - passes with assertions
- [ ] Test 2: Successful complaint submission - form validates and submits
- [ ] Test 3: Search for existing complaint - returns matching results
- [ ] Test 4: Authorized status update - valid transition works
- [ ] Test 5: Unauthorized access restriction - role blocks access
- [ ] Each test includes useful failure screenshots
- [ ] Tests run via `./mvnw test`
- [ ] Failed tests stop deployment (quality gate)

### TS-13 (Docker)
- [ ] Dockerfile builds successful image
- [ ] Docker Compose brings up MySQL + application
- [ ] Application connects to MySQL successfully
- [ ] Health check endpoint `/actuator/health` responds
- [ ] Container ports exposed and documented
- [ ] `docker compose down` / `up` works
- [ ] Rollback procedure documented (restore previous image)

### TS-14 (Jenkins)
- [ ] Jenkinsfile with all 12 stages present
- [ ] Stage 1: Checkout from Git
- [ ] Stage 2: Environment and Java version verification
- [ ] Stage 3: Maven clean and compile
- [ ] Stage 4: Unit tests execute
- [ ] Stage 5: Integration tests execute
- [ ] Stage 6: Package JAR artifact
- [ ] Stage 7: Archive JAR artifact
- [ ] Stage 8: Build Docker image
- [ ] Stage 9: Run Selenium tests against deployed app
- [ ] Stage 10: Publish test reports
- [ ] Stage 11: Deploy only if quality gates pass
- [ ] Stage 12: Deployment health check
- [ ] Pipeline triggered on Git commit (webhook or polling)
- [ ] Failed tests block Docker build and deployment

### TS-15 (Ansible)
- [ ] Inventory file specifies target host
- [ ] Variables file has placeholder values (not secrets)
- [ ] Playbook installs required packages (Java, Docker, etc.)
- [ ] Playbook creates deployment directories
- [ ] Playbook copies application configuration
- [ ] Playbook starts Docker Compose or restarts service
- [ ] Health check verifies application responsiveness
- [ ] Second playbook run shows idempotency (`--changed=0`)
- [ ] Rollback/recovery procedure documented

## Prioritization Notes

- **High Priority**: Features used by all roles (auth, CRUD, dashboard, basic search)
- **Medium Priority**: Role-specific features (technician assignments, admin management)
- **Low Priority**: Out-of-scope features (email, file attachments, advanced reporting)

- User stories ordered by business value and implementation dependency
- TS-1 through TS-5 must complete before TS-6 through TS-8 can proceed
- TS-12 (Selenium) depends on TS-8 having working application
- TS-13 (Docker) depends on TS-8 having compilable application
- TS-14 (Jenkins) depends on TS-13 having working Docker setup
- TS-15 (Ansible) depends on TS-13 and target environment availability

## Backlog Grooming

- Stories re-prioritized each sprint based on feedback
- Definition of Done applies to all stories (code, tests, docs, demo)
- Spikes for unclear requirements estimated separately
- Dependencies tracked and visualized in Kanban board