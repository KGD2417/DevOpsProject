# Limitations and Future Scope

## 1. Current Limitations

### 1.1 Functional Limitations

| # | Limitation | Impact | Workaround |
|---|------------|--------|------------|
| **L-1** | No email notification system | Citizens/technicians/admins don't receive email alerts on status changes | Can be added later via Spring Mail/javamail; not critical for MVP |
| **L-2** | No file attachments (photos of faulty streetlights) | Citizens cannot upload images of the fault | Future enhancement; can use AWS S3 or local file storage later |
| **L-3** | No comments/discussion thread on complaints | No way for users to add additional information to a complaint | Could be added as a separate entity with one-to-one relation to complaints |
| **L-4** | Limited to 3 user roles (Citizen, Technician, Admin) | No role-specific sub-roles (e.g., "Senior Technician", "Area Manager") | Role hierarchy can be added later; current 3 roles suffice for MVP |
| **L-5** | No bulk operations | Cannot assign multiple technicians or update status for multiple complaints at once | Manual one-by-one operations suffice for demo; bulk can be added later |
| **L-6** | No search by date range | Search available by ID, ward, category, status, priority only | Date range search can be added using `reported_at` BETWEEN parameters |
| **L-7** | No API rate limiting (beyond basic throttling)** | No protection against API abuse; may be vulnerable to brute force | Can add Spring Security `RequestLimiter` or Guava rate limiter later |
| **L-8** | No multi-tenancy | Single instance serves all users; no data isolation per municipality | Future: database-level or schema-level multi-tenancy; not needed for college project |
| **L-9** | Dashboard metrics only (no trends/graphs)** | Shows current counts; no time-series charts or trend analysis | Can be added using Chart.js, Highcharts; requires time-stored data |
| **L-10** | Limited localization (English only)** | No language localization; all UI text in English | Future: resource bundles; i18n support; not critical for single-locale demo |

### 1.2 Technical Limitations

| # | Limitation | Impact | Workaround |
|---|------------|--------|------------|
| **L-11** | H2 database for tests only (not production-ready)** | Tests use H2 in-memory; production uses MySQL 8 | Entity annotations compatible; data migration script ensures schema parity |
| **L-12** | No Kubernetes, Docker Swarm orchestration** | Single-container/docker-compose only; no auto-scaling | Docker Compose sufficient for college project; Kubernetes can be added later |
| **L-13** | No HTTPS/SSL by default (HTTP only)** | Application runs over HTTP; no TLS/SSL termination | Can add Nginx reverse proxy with self-signed cert or Let's Encrypt later |
| **L-14** | Session-based authentication (no JWT)** | Stateless auth not used; session cookies | Session-based is simpler for college project; JWT can be added later |
| **L-15** | No API versioning** | All endpoints under same version; backward compatibility not enforced | Can add `/api/v1/` prefix later; not critical for MVP |
| **L-16** | Fixed database column sizes** | VARCHAR(50), TEXT without max length; may need adjustment for larger data | Can alter table; entity annotations can be updated |
| **L-17** | No background job processing** | All operations synchronous; no async task queue | RabbitMQ, Redis, or SQS can be added for async processing later |
| **L-18** | Limited error logging structure** | Application logs to console/file; no centralized log aggregation | Can add ELK stack, Splunk, or Logback centralized logging later |
| **L-19** | Single-point-of-failure deployment** | Docker Compose on one host; no high availability | Can add multiple instances, load balancer; Kubernetes Horizontally Scaling later |
| **L-20** | No automated backup system** | Database backups manual; no scheduled backup jobs | Can add cron jobs + mysqldump; cloud database snapshots later |

### 1.3 Design Limitations

| # | Limitation | Impact | Workaround |
|---|------------|--------|------------|
| **L-21** | Bootstrap 5 responsive design only; minimal custom CSS** | UI looks consistent but not highly customized | Bootstrap 5 provides good out-of-box responsiveness; minor CSS can customize later |
| **L-22** | Minimal JavaScript** | No complex client-side logic; DOM updates via Thymeleaf fragment replacement | Essential JS only for form validation; complex interactions server-side rendered |
| **L-23** | Dashboard cards hardcoded colors/statistics** | Colors use Bootstrap defaults; no custom color schemes | Can add custom CSS; statistics computed from database (as designed) |
| **L-24** | No accessibility audit (WCAG 2.1)** | May not fully meet accessibility standards | Basic ARIA labels; color contrast; can audit and fix later with axe,lighthouse tools |
| **L-25** | Error pages generic (no custom 404/500 pages beyond basic)** | Spring Boot default error pages; not branded | Can create `error/` Thymeleaf templates for customized error pages |

## 2. Future Enhancements

### 2.1 Priority Enhancements (High Value)

| # | Enhancement | Description | Estimated Effort |
|---|-------------|-------------|------------------|
| **E-1** | Email notifications | Automated emails on status changes: citizen complaint received, technician assigned, admin resolved; use Spring Mail with javamail; SMTP configuration | Medium (2-3 weeks) |
| **E-2** | File attachments (photos) | Allow citizens to upload JPEG/PNG images of faulty streetlight; store in filesystem or AWS S3; show thumbnail on complaint detail | Medium (3-4 weeks) |
| **E-3** | Advanced search with date range | Add "From Date" / "To Date" picker; filter complaints by reporting date range; SQL: `WHERE reported_at BETWEEN :from AND :to` | Low (1 week) |
| **E-4** | Role-based sub-permissions | Fine-grained access: "Can assign technician", "Can reopen resolved", "Can delete own complaints only" | Medium (2-3 weeks) |
| **E-5** | Resolution rate trends over time | Line chart showing resolution rate by week/month; calculate from status_history timestamps | Low (1 week) |

### 2.2 Medium Priority Enhancements

| # | Enhancement | Description | Estimated Effort |
|---|-------------|-------------|------------------|
| **E-6** | User profile management | Users can update password, change username (admin can edit others); profile page with personal info | Low (1-2 weeks) |
| **E-7** | Complaint status filtering on dashboard | Dropdown to filter dashboard by specific status; show "X open, Y in progress" with filters | Low (1 week) |
| **E-8** | Printable complaint reports | Generate PDF report of complaint with all details, history, images; use Thymeleaf + PDF template (itext, pdfbox) | Medium (2 weeks) |
| **E-9** | Mobile optimization enhancements | Beyond Bootstrap 5; custom viewport, touch-friendly elements; specific mobile CSS | Low (1 week) |
| **E-10** | Internationalization (i18n)** | Resource bundles for English/Spanish/French; localized date/number formatting; i18n Thymeleaf attributes | Medium (3-4 weeks) |

### 2.3 Lower Priority Enhancements

| # | Enhancement | Description | Estimated Effort |
|---|-------------|-------------|------------------|
| **E-11** | Advanced dashboard analytics | KPI widgets: mean time to repair, complaints per ward per month, peak fault hours | Medium (3 weeks) |
| **E-12** | Audit log beyond status history | Log all user actions (login, logout, profile changes, configuration changes); syslog integration | High (4+ weeks) |
| **E-13** | GIS/map integration | Plot complaints on map (Leaflet, Google Maps) by ward/street; spatial query capabilities | High (4-5 weeks) |
| **E-14** | Export/import data | CSV export of complaints for external analysis; import for bulk data load | Low (1 week) |
| **E-15** | User activity timeline | Show user's activity history: complaints submitted, status updates performed, actions taken | Low (1-2 weeks) |

### 2.4 Architecture Enhancements (Future Versions)

| # | Enhancement | Description |
|---|-------------|-------------|
| **A-1** | Migration to Kubernetes | Container orchestration; auto-scaling; Helm charts; multi-environment deployment |
| **A-2** | Microservices breakdown | Split into: auth-service, complaint-service, notification-service, dashboard-service |
| **A-3** | Cloud deployment (AWS/Azure/GCP) | RDS for MySQL; S3 for file storage; ECS/Fargate or Cloud Run for containers |
| **A-4** | CI/CD pipeline enhancements | Argo CD for GitOps; multi-stage pipelines; canary deployments; automated canary analysis |
| **A-5** | Database clustering | Read replicas; connection pooling; automated failover; AWS Aurora or equivalent |
| **A-6** | Advanced security features | OAuth2 + OpenID Connect; role-based access control (RBAC) beyond simple ENUM; session management |

### 2.5 Viva/Project Presentation Enhancements

| # | Enhancement | Purpose |
|---|-------------|---------|
| **V-1** | Demonstration script with live demo | Prepared script covering key workflows: citizen login → submit → technician update → admin dashboard |
| **V-2** | Performance benchmarks | Response time measurements; database query performance; Docker resource usage |
| **V-3** | Security demonstration | Show role-based access restriction; attempt unauthorized access (blocked by security); password hashing verification |
| **V-4** | DevOps pipeline walkthrough | Show Jenkins pipeline stages; Docker build/deploy; Ansible provisioning demo; test results |
| **V-5** | Troubleshooting scenarios | Document 3-5 common issues and how they were resolved; demonstrate fix commands |
| **V-6** | CHANGELOG and release notes | Show version progression; what was added in each milestone; known limitations acknowledged |

## 3. Summary

### 3.1 What the MVP Delivers

The Minimum Viable Product delivers a fully functional streetlight fault reporting system with:

- ✅ Three-role authentication (Citizen, Technician, Admin)
- ✅ Complete complaint CRUD with validation
- ✅ Controlled status workflow with audit trail
- ✅ Search and filtering by multiple criteria
- ✅ Summary dashboard with real database metrics
- ✅ Role-based access control (server-side enforcement)
- ✅ Docker containerization with MySQL + Nginx
- ✅ Jenkins CI/CD pipeline with Selenium quality gates
- ✅ Ansible configuration management with idempotency
- ✅ 18 required documentation files
- ✅ Mermaid diagrams for architecture, use cases, ERD, DevOps lifecycle
- ✅ Git branching strategy with conventional commits
- ✅ Version tag `v1.0.0` for release baseline

### 3.2 Known Limitations (Concise)

The system has these documented limitations (see `docs/limitations-and-future-scope.md`):

1. No email notifications
2. No file attachments
3. No date-range search
4. H2 tests database only (MySQL for production)
5. No Kubernetes/Docker Swarm
6. HTTP only (no HTTPS by default)
7. Session-based auth (no JWT)
8. No API versioning
9. Dashboard metrics only (no trends)
10. English-only UI

### 3.3 Future Roadmap

The project is designed to evolve from MVP with these planned enhancements:

- Email notifications (high value)
- File attachments (medium value)
- Date-range search (low value)
- Role sub-permissions (medium value)
- Resolution rate trends (low value)
- And more enhancements in the roadmap

The architecture is modular enough to accommodate these additions without refactoring core functionality.

## 4. Viva Preparation Notes

### 4.1 Likely Viva Questions and Answers

| # | Question | Concise Answer |
|---|----------|----------------|
| **Q-1** | "Explain the architecture of your application." | Three-layer architecture: Controller → Service → Repository → Entity → MySQL. Spring Boot 3.x with Spring MVC, Thymeleaf for views, Spring Data JPA/Hibernate, MySQL 8, Flyway migrations. |
| **Q-2** | "How do you handle role-based authorization?" | Spring Security with `BCryptPasswordEncoder` for password hashing. `Role` ENUM (CITIZEN, TECHNICIAN, ADMIN). `@PreAuthorize` annotations on controller methods. Thymeleaf `sec:authorize` tags hide/show UI elements. Server-side enforcement (not just hiding buttons in frontend). |
| **Q-3** | "Explain the status workflow and how you enforce valid transitions." | Controlled workflow: REPORTED → ACKNOWLEDGED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED. Server-side validation in `ComplaintService`; only allowed transitions accepted; invalid transitions rejected with error message. Status history table records: previous status, new status, timestamp, user who made change. |
| **Q-4** | "How does the dashboard compute its values?" | All dashboard values computed from actual database records via repository methods. `SELECT COUNT(*) FROM complaints WHERE status != 'CLOSED'` for open complaints; similar queries for assigned, in-progress, resolved, high-priority. Resolution rate = RESOLVED / total * 100. No hardcoded values. |
| **Q-5** | "How does the CI/CD pipeline work?" | Jenkins Pipeline (Jenkinsfile) with 12 stages: checkout → environment verify → maven clean/compile → unit tests → integration tests → package JAR → archive JAR → build Docker image → Selenium tests → publish reports → deploy if gates pass → health check. Failed tests block Docker build and deployment. Triggered on Git commits via GitHub webhook or polling. |
| **Q-6** | "How do you ensure Docker deployment is idempotent and recoverable?" | Docker Compose with health checks; rollback to previous image tag if health check fails; Ansible playbook runs idempotently (`changed=0` on second run); Jenkins health check stage triggers rollback on failure; non-root execution in Docker images. |
| **Q-7** | "Explain the database design and key entities." | Three entities: User (id, username, BCrypted password, role, timestamps), Complaint (id, complaintId, reporter, assignedTo, streetName, ward, faultCategory, description, priority, status, timestamps, resolution notes), StatusHistory (id, complaintId, previousStatus, newStatus, changedAt, changedBy). Foreign key constraints, indexes on status/priority/ward, ENUM constraints. Flyway V1__initial_schema.sql for migration. |
| **Q-8** | "How do you run and manage automated tests?" | JUnit 5 + Mockito for unit tests; Spring Boot Test + H2 for integration tests; Selenium WebDriver for 5 user journey tests (login, submit complaint, search, status update, unauthorized restriction). Explicit waits over `Thread.sleep()`. Failed tests block deployment in Jenkins. Run locally with `./mvnw test`. |
| **Q-9** | "What challenges did you face and how did you overcome them?" | Typical answers: Maven not installed → used Maven Wrapper; Docker Compose networking → studied documentation; Selenium in CI → headless Chrome with `--no-sandbox`; merge conflicts → resolved with `git mergetab`; Ansible idempotency → used idempotent modules (`file`, `service`, `apt`); default admin credentials → documented as development-only with password change on first login. |
| **Q-10** | "Show me how you would deploy this to production." | `docker compose -f docker-compose.yml up -d` with MySQL + app + Nginx; environment variables via `.env`; health check `/actuator/health`; domain with SSL via Nginx; backup strategy; monitoring; rollback procedure (restore previous Docker image tag). |

### 4.2 Presentation Structure (15-Minute Viva)

| Section | Time | Content |
|---------|------|---------|
| **Introduction** | 1 min | Project name; problem statement; stakeholders |
| **Architecture** | 2 min | Layered architecture diagram; technology stack; Mermaid diagram |
| **Database** | 2 min | ER diagram; key entities; relationships; Flyway migration |
| **Security** | 2 min | Authentication; role-based authorization; BCrypt hashing; server-side enforcement |
| **Workflow** | 2 min | Status workflow; transition validation; status history |
| **DevOps** | 3 min | Jenkins pipeline (12 stages); Docker Compose; Ansible provisioning; CI/CD workflow |
| **Testing** | 2 min | Unit tests; integration tests; Selenium 5 user journeys; test evidence |
| **Documentation** | 2 min | 18 required documents; Mermaid diagrams; SRS; user stories; acceptance criteria |
| **Demo** | 2 min | Live or recorded demonstration of key workflows |
| **Q&A** | 2 min | Answer questions from evaluators |

### 4.3 Demonstration Script (Key Workflows)

1. **Citizen Registration & Login**: "I'll register a new citizen account, then log in with those credentials."
2. **Complaint Submission**: "As a citizen, I'll submit a new streetlight fault complaint - selecting the street, ward, fault category, description, and priority."
3. **Search and Filtering**: "I'll search for complaints by ward or by fault category to find specific reports."
4. **Status Update (Technician)**: "As a technician, I'll update the complaint status through the valid workflow transitions."
5. **Admin Management**: "As an admin, I'll assign a technician to a complaint, view all complaints, and access the summary dashboard."
6. **Dashboard Overview**: "The dashboard shows real-time metrics computed from the database: total complaints, open cases, assignment status, resolution rate, and recent activity."
7. **Role Restriction**: "I'll demonstrate that a citizen cannot access admin-only functions - the server enforces authorization regardless of frontend button visibility."

### 4.4 Viva Day Checklist

- [ ] All 18 documentation files present and populated
- [ ] Mermaid diagrams generated and readable (architecture, use-case, ERD, DevOps lifecycle)
- [ ] Git history shows meaningful commits across branches (main/develop/feature/bugfix)
- [ ] Version tag `v1.0.0` created and annotated on `main`
- [ ] Docker Compose brings up all services (MySQL + app + Nginx)
- [ ] Application accessible at `http://localhost:8080` or nginx URL
- [ ] Selenium tests pass (all 5 user journeys)
- [ ] Jenkins pipeline runs successfully (all 12 stages)
- [ ] Ansible playbook runs idempotently (second run: `changed=0`)
- [ ] Default admin credentials changed (or documented as development-only)
- [ ] `.env` file has real values; `.env.example` has placeholders only
- [ ] No credentials, secrets, or passwords committed to Git (verified via `.gitignore`)
- [ ] Project compiles: `./mvnw clean compile` succeeds
- [ ] Tests run: `./mvnw test` succeeds
- [ ] Demonstration script prepared and rehearsed
- [ ] Slides or presentation materials ready
- [ ] Troubleshooting guide reviewed (common issues and solutions)
- [ ] Known limitations acknowledged and discussed
- [ ] Future enhancements noted (roadmap)

---