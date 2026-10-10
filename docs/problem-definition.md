# Automated Streetlight Fault Reporting System

## Problem Definition

The Automated Streetlight Fault Reporting System addresses the problem of inefficient streetlight maintenance in municipal areas. Currently, citizens have no streamlined way to report faulty streetlights, and municipal staff lack an efficient system to track, prioritize, and resolve reported faults. This results in prolonged downtime for critical street lighting, safety hazards, and poor resource allocation for repairs.

### Stakeholders
- **Citizens**: Report streetlight faults, track complaint status, receive updates on repair progress
- **Technicians**: Receive assigned complaints, update repair progress, mark complaints as resolved
- **Admins**: Manage complaints, assign technicians, control workflow, view dashboards, generate reports
- **Municipality**: Track maintenance metrics, optimize resource allocation, improve public safety

### Constraints
- Must use Java 21 with Spring Boot 3.x
- Must use Spring Security with role-based authorization
- Must use MySQL 8 as primary database, H2 for local testing
- Must use Thymeleaf for server-side rendering
- Must follow secure coding practices (password hashing, validation, authorization)
- DevOps lifecycle must include CI/CD, Docker, Ansible, and automated testing
- Must be suitable for college project demonstration (viva preparation)
- Budget constraints: no paid APIs, no Kubernetes, no microservices

### Objectives
- Enable citizens to report streetlight faults via web interface
- Allow technicians to update repair progress and mark resolutions
- Allow admins to manage workflow, assign technicians, and view analytics
- Provide role-based access control (Citizen / Technician / Admin)
- Track complete complaint status history with timestamps
- Provide summary dashboard with key metrics (total, open, resolved, high-priority)
- Support search and filtering by location, ward, category, status, priority
- Deploy as containerized application with Docker and Docker Compose
- Implement Jenkins CI/CD pipeline with automated Selenium tests
- Provision infrastructure via Ansible playbooks

### Measurable Success Criteria
- **Functional**: 100% of CRUD operations work for all user roles
- **Security**: Unauthorized users cannot access admin/technician endpoints; role-based button hiding supplemented with server-side authorization
- **Usability**: Dashboard displays correct counts from database; search/filter returns relevant results
- **Testing**: All 5 Selenium user journeys pass (login, submit complaint, search, status update, unauthorized restriction)
- **Build**: Maven build compiles and runs unit tests successfully
- **Container**: Docker image builds and runs; Docker Compose brings up MySQL + app stack
- **Deployment**: Jenkins pipeline runs end-to-end: build → test → package → Docker → health check
- **Ansible**: Playbook runs idempotently twice with no unnecessary changes
- **Viva**: Student can explain complete architecture, data flow, DevOps pipeline, and answer technical questions

## Stakeholders and Objectives

### Primary Stakeholders

| Stakeholder | Role | Responsibilities |
|------------|------|------------------|
| Citizens | Reporter | Submit complaints, view own reports, track status, receive notifications |
| Technicians | Repair Staff | View assigned complaints, update progress (IN_PROGRESS → RESOLVED), add resolution notes |
| Admins | Municipal Staff | Manage all complaints, assign technicians, change workflow status, view dashboard, generate reports |
| Municipality | Organization | Track metrics, optimize SLA compliance, improve public safety |

### stakeholder Objectives

- **Citizens**: Quick fault reporting (≤5 minutes), clear status visibility, mobile-friendly interface
- **Technicians**: Organized task assignment, clear progress tracking, efficient status updates
- **Admins**: Comprehensive oversight, workflow control, metrics for decision-making
- **Municipality**: Reduced mean time to repair, better resource allocation, improved citizen satisfaction

### Constraints and Assumptions

#### Constraints
1. **Technology Stack**: Java 21, Spring Boot 3.x, Thymeleaf, MySQL 8, Maven, Docker, Docker Compose, Jenkins, Ansible
2. **No Paid Services**: No external paid APIs, no Kubernetes, no microservices architecture
3. **Security**: Spring Security with form login, BCrypt password hashing, role-based URL access
4. **Deployment**: Docker Desktop or equivalent, Jenkins available or locally runnable
5. **Single Instance**: No clustering or distributed deployment required

#### Assumptions
1. **Environment**: Developer has Java 21+, Maven 3.9.x+, Docker Desktop, Git
2. **Target Users**: College students and municipal staff with basic technical literacy
3. **Concurrent Users**: Designed for typical municipal workload, not massive concurrent scale
4. **Data Privacy**: No sensitive personal data beyond name and contact info; passwords hashed
5. **Local Development**: All components run locally during development and testing

### Measurable Success Criteria (Detailed)

| Category | Metric | Target |
|----------|--------|--------|
| **Functionality** | Complaint CRUD operations | All create, read, update, delete operations work for authorized roles |
| **Security** | Role-based access | Admin/Technician endpoints inaccessible to Citizen role without proper authorization |
| **Usability** | Dashboard accuracy | All dashboard values computed from actual database records |
| **Testing** | Selenium test suite | All 5 user journeys pass: login, submit complaint, search, status update, unauthorized access |
| **Build** | Maven build time | Compile + unit tests complete within 10 minutes |
| **Container** | Docker image size | Base image ≤ 200MB for application layer |
| **CI/CD** | Pipeline quality gates | Failed tests block Docker build and deployment |
| **Ansible** | Idempotency | Running playbook twice produces no unnecessary changes |
| **Viva Preparation** | Student explanation | Can explain architecture, data flow, DevOps pipeline, security implementation |

### Success Measurement Approach

1. **Unit Tests**: JUnit 5 + Mockito test complaint repository, service layer, security config
2. **Integration Tests**: Spring Boot Test with embedded H2 database
3. **Selenium Tests**: 5 user journeys with assertions and failure screenshots
4. **Docker Verification**: Container starts, health check endpoint responds, database connects
5. **Ansible Verification**: `ansible-playbook` run twice; second run shows `--changed=0` or only idempotent changes
6. **Jenkins Verification**: Pipeline completes stages 1-12 successfully with test reports archived

---