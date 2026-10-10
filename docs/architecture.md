# Architecture Overview

## 1. Layered Architecture

The application follows a maintainable layered architecture with clear separation of concerns:

```
+--------------------------------------------------------------+
|              Presentation Layer (Thymeleaf)                   |
|  - HTML templates with Bootstrap 5                           |
|  - Form processing                                           |
|  - Role-based visibility (`sec:authorize`)                   |
|  - Message display (success, error, validation)              |
+---------------------+----------------------------------------+
|         Controller Layer (Spring MVC)                         |
|  - HTTP request handling                                      |
|  - Request parameter binding                                  |
|  - Model preparation for templates                            |
|  - Redirection and view resolution                            |
+---------------------+----------------------------------------+
|         Service Layer (Business Logic)                        |
|  - Complaint business rules                                   |
|  - Status workflow enforcement                                |
|  - Status history recording                                   |
|  - Validation orchestration                                   |
|  - Search and filter logic                                    |
+---------------------+----------------------------------------+
|         Repository Layer (Spring Data JPA)                    |
|  - JPA entity management                                      |
|  - Database CRUD operations                                   |
|  - Custom query methods (search/filter)                       |
|  - Pagination support                                         |
+---------------------+----------------------------------------+
|             Entity Layer                                    |
|  - User (with Role enum)                                      |
|  - Complaint (with all fields, ENUMs)                        |
|  - StatusHistory (audit trail)                                |
|  - Relationships: User→Complaint, Complaint→StatusHistory   |
+--------------------------------------------------------------+
                    |
                    v
+--------------------------------------------------------------+
|          Database Layer (MySQL 8)                             |
|  - Tables: users, complaints, status_history                  |
|  - Indexes: status, priority, ward, complaintId               |
|  - Triggers: created_at, updated_at auto-timestamps           |
|  - Constraints: PK, FK, UNIQUE, NOT NULL, ENUM checks       |
+--------------------------------------------------------------+
```

## 2. Technology Architecture

### 2.1 Software Stack

| Layer | Technology | Version | Purpose |
|-------|------------|---------|---------|
| **Language** | Java | 21 | Target language |
| **Framework** | Spring Boot | 3.x | Application framework |
| **Web** | Spring MVC | bundled | Servlet handling |
| **Templates** | Thymeleaf | 3.x | Server-side rendering |
| **CSS** | Bootstrap 5 | 5.x | Responsive styling |
| **JS** | Minimal | — | occasional DOM updates |
| **Build** | Maven | 3.9.x | Project build |
| **Dependency** | Spring Data JPA + Hibernate | bundled | ORM |
| **Validation** | Jakarta Bean Validation | 3.0 | Constraint validation |
| **Security** | Spring Security | 6.x | Authentication & authorization |
| **Database** | MySQL | 8.0 | Primary data store |
| **Test DB** | H2 | 2.2+ | Local unit tests |
| **Testing** | JUnit 5 | 5.x | Unit tests |
| **Mocking** | Mockito | 5.x | Mock objects |
| **Integration** | Spring Boot Test | 3.x | Integration tests |
| **Selenium** | Selenium WebDriver | 4.x | Browser automation |
| **Container** | Docker | 28.5.1 | Application containerization |
| **Orchestration** | Docker Compose | v2.40.2 | Multi-container |
| **Reverse Proxy** | Nginx | 1.24+ | Static assets, SSL termination |
| **CI/CD** | Jenkins | LTS | Pipeline automation |
| **Infrastructure** | Ansible | 9.x+ | Configuration management |
| **Repository** | Git | 2.56+ | Version control |
| **Git Hosting** | GitHub | Remote |
| **Registry** | Docker Hub | Image repository |

### 2.2 Configuration Architecture

#### 2.2.1 Application Configuration (`application.yml`)

```yaml
spring:
  application:
    name: streetlight-fault-reporting
  datasource:
    url: jdbc:mysql://localhost:3306/streetlight?useSSL=false&serverTimezone=UTC
    username: streetlight
    password: ${DB_PASSWORD:streetlight123}
    hikari:
      maximum-pool-size: 10
    initialization-mode: always
  jpa:
    hibernate:
      ddl-auto: none  # Flyway migrations used instead
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
        format_sql: false
    generate-ddl: false
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    baseline-version: "0"
  profiles:
    active: dev  # or prod, test

  # Actuator health check
 :
 :
```

#### 2.2.2 Environment Configuration (`.env`)

```
# Database
DB_HOST=localhost
DB_PORT=3306
DB_NAME=streetlight
DB_USER=streetlight
DB_PASSWORD=your_password_here

# Application
APP_NAME=streetlight-fault-reporting
APP_URL=http://localhost:8080
SERVER_PORT=8080

# Security
BCRYPT_STRENGTH=10

# Docker
DOCKER_IMAGE=streetlight/fault-reporting
DOCKER_TAG=latest
```

#### 2.2.3 `.env.example` (Committed to Repo)

```
# Database - CHANGE THESE FOR PRODUCTION
DB_HOST=localhost
DB_PORT=3306
DB_NAME=streetlight
DB_USER=streetlight
DB_PASSWORD=CHANGE_ME_PRODUCTION

# Application
APP_NAME=streetlight-fault-reporting
APP_URL=http://localhost:8080
SERVER_PORT=8080

# Security
BCRYPT_STRENGTH=10

# Docker
DOCKER_IMAGE=streetlight/fault-reporting
DOCKER_TAG=dev
```

### 2.3 Deployment Architecture

```
+-----------------------------------------------------------------+
|                    Docker Compose Architecture                   |
+-----------------------------------------------------------------+
|                      (docker compose up -d)                     |
|                                                                 |
|  +----------------------+    +----------------------+     |
|  |        Nginx         |    |        MySQL         |     |
|  |  (Reverse Proxy)    |    |  (Database Service)  |     |
|  |  Port 80/443        |    |  Port 3306           |     |
|  |  - Static assets    |    |  - Persistent data   |     |
|  |  - SSL termination  |    |  - 3306/tcp          |     |
|  +----------+----------+    +----------+----------+     |
|            |                          |               |
|            |                          |               |
|  +----------v----------+  +--------v----------+  |
|  |   Application      |  |   Application   |  |
|  |   Service (Tomcat) |  |    Tests       |  |
|  |  Port 8080         |  |  (H2 in-memory)|  |
|  +----------+----------+  +--------+----------+  |
|            |                          |               |
|            +----------------------------+---------------+
                                        |
                                        v
+-----------------------------------------------------------------+
|                   Jenkins Pipeline (CI/CD)                      |
+-----------------------------------------------------------------+
|  Stages:  1. Checkout   2. Verify env   3. Maven clean   |
|           4. Unit tests   5. Integration tests   6. Package |
|           7. Archive JAR   8. Build Docker image   |
|           9. Selenium tests   10. Publish reports   |
|           11. Deploy if gates pass   12. Health check |
+-----------------------------------------------------------------+
```

## 3. Entity Relationship Model

```
+---------------------+       +---------------------+       +---------------------+
|         User        |       |     Complaint       |       |    StatusHistory    |
+---------------------+       +---------------------+       +---------------------+
| id (PK)             |-------| id (PK)             |-------| id (PK)             |
| username (UK, NN)  |       | complaintId (UK, NN)|       | complaint_id (FK)   |
| password (NN)       |       | streetName (NN)     |       | previousStatus (NN) |
| role (ENUM, NN)     |       | ward (NN)           |       | newStatus (NN)      |
| created_at (NN)     |       | faultCategory (NN)  |       | changedAt (NN, default now) |
| updated_at (NN)     |       | description (NN)    |       | changedBy (FK)      |
|                     |       | priority (ENUM, NN) |       |                     |
+---------------------+       | reporter (FK)       |       +---------------------+
                                | assignedTo (FK)   |
                                | status (ENUM, NN) |
                                | resolvedAt (NN, null)|
                                | resolutionNotes (TEXT, null)|
                                | createdAt (default now)|
                                | lastUpdated (default now)|
+---------------------+       +---------------------+
        |                               |
        | 1-(N)                         | 1-(N)
        |                               |
        v                               v
```

### 3.1 Detailed Relationships

#### 3.1.1 User ↔ Complaint (1:N)
- **One user** can submit **many complaints**
- `complaint.reporter` → `User` (many complaints share the same reporter)
- `complaint.assignedTo` → `User` (technician assignment, nullable)
- Foreign key: `complaint.reporter_id` references `user.id`
- Foreign key: `complaint.assignedTo_id` references `user.id`
- On DELETE: SET NULL for assignedTo (technician leaves, complaint reassigned)

#### 3.1.2 Complaint ↔ StatusHistory (1:N)
- **One complaint** has **many status history entries**
- `statusHistory.complaint` → `Complaint` (all transitions for one complaint)
- Foreign key: `statusHistory.complaint_id` references `complaint.id`
- On each status change: new row in status_history with:
  - `previousStatus` (the state before transition)
  - `newStatus` (the state after transition)
  - `changedAt` (timestamp, auto-generated)
  - `changedBy` (the user who made the change)

#### 3.1.3 User ↔ Role (1:1)
- **One user** has **one role**
- `user.role` → `Role` enum (CITIZEN, TECHNICIAN, ADMIN)
- Role determines authorized endpoints and Thymeleaf `sec:authorize` access

### 3.2 Database Schema Design

#### 3.2.1 Users Table

```sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    role ENUM('CITIZEN', 'TECHNICIAN', 'ADMIN') NOT NULL DEFAULT 'CITIZEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);
```

**Indexes**:
- `UNIQUE INDEX idx_username (username)`
- `INDEX idx_role (role)`

#### 3.2.2 Complaints Table

```sql
CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(20) NOT NULL UNIQUE,
    reporter_id BIGINT NOT NULL,
    assigned_to BIGINT NULL,
    street_name VARCHAR(100) NOT NULL,
    ward VARCHAR(50) NOT NULL,
    fault_category ENUM('NOT_WORKING', 'FLICKERING', 'DAMAGED_POLE', 'EXPOSED_WIRING', 'OTHER') NOT NULL,
    description TEXT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'EMERGENCY') NOT NULL,
    status ENUM('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'REPORTED',
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
);
```

**Indexes**:
- `INDEX idx_status (status)`
- `INDEX idx_priority (priority)`
- `INDEX idx_ward (ward)`
- `INDEX idx_complaint_id (complaint_id)`
- `INDEX idx_reporter (reporter_id)`
- `INDEX idx_assigned (assigned_to)`
- `INDEX idx_status_priority (status, priority)` -- composite for dashboard queries

#### 3.2.3 Status History Table

```sql
CREATE TABLE status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    previous_status ENUM('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL,
    new_status ENUM('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by BIGINT NOT NULL,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
);
```

**Indexes**:
- `INDEX idx_complaint (complaint_id)`
- `INDEX idx_changed_by (changed_by)`
- `INDEX idx_timestamp (changed_at)`
- `INDEX idx_status_previous (previous_status)`
- `INDEX idx_status_new (new_status)`

#### 3.2.4 ENUM Constraints

```sql
-- Valid fault categories
CHECK (fault_category IN ('NOT_WORKING', 'FLICKERING', 'DAMAGED_POLE', 'EXPOSED_WIRING', 'OTHER'));

-- Valid priorities
CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'EMERGENCY'));

-- Valid statuses
CHECK (status IN ('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'));

-- Valid roles
CHECK (role IN ('CITIZEN', 'TECHNICIAN', 'ADMIN'));

-- Valid previous/status transitions enforced by application logic (see workflow)
```

#### 3.2.5 Flyway Migration Script (V1__initial_schema.sql)

```sql
-- V1__initial_schema.sql
CREATE TABLE users (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    username VARCHAR(50) NOT NULL UNIQUE,
    password VARCHAR(128) NOT NULL,
    role ENUM('CITIZEN', 'TECHNICIAN', 'ADMIN') NOT NULL DEFAULT 'CITIZEN',
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE complaints (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id VARCHAR(20) NOT NULL UNIQUE,
    reporter_id BIGINT NOT NULL,
    assigned_to BIGINT NULL,
    street_name VARCHAR(100) NOT NULL,
    ward VARCHAR(50) NOT NULL,
    fault_category ENUM('NOT_WORKING', 'FLICKERING', 'DAMAGED_POLE', 'EXPOSED_WIRING', 'OTHER') NOT NULL,
    description TEXT NOT NULL,
    priority ENUM('LOW', 'MEDIUM', 'HIGH', 'EMERGENCY') NOT NULL,
    status ENUM('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL DEFAULT 'REPORTED',
    resolved_at TIMESTAMP NULL,
    resolution_notes TEXT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    last_updated TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    FOREIGN KEY (reporter_id) REFERENCES users(id) ON DELETE RESTRICT,
    FOREIGN KEY (assigned_to) REFERENCES users(id) ON DELETE SET NULL
);

CREATE TABLE status_history (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    complaint_id BIGINT NOT NULL,
    previous_status ENUM('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL,
    new_status ENUM('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED') NOT NULL,
    changed_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    changed_by BIGINT NOT NULL,
    FOREIGN KEY (complaint_id) REFERENCES complaints(id) ON DELETE CASCADE,
    FOREIGN KEY (changed_by) REFERENCES users(id) ON DELETE RESTRICT
);
```

## 4. API Reference Summary

### 4.1 Base URL
`http://localhost:8080/api/` (internal) or `http://localhost:8080/` (Thymeleaf views)

### 4.2 Authentication Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/login` | Login form | Unauthenticated |
| POST | `/login` | Process login | Unauthenticated |
| GET | `/logout` | Logout | Authenticated |

### 4.3 Complaint Endpoints (Role-Based)

| Method | Endpoint | Description | Citizen | Technician | Admin |
|--------|----------|-------------|---------|------------|-------|
| GET | `/complaints` | List complaints | My complaints | My + ward | All complaints |
| GET | `/complaints/new` | Create form | Yes | No | Yes |
| POST | `/complaints` | Create complaint | Yes | No | Yes |
| GET | `/complaints/{id}` | View detail | Yes | Yes | Yes |
| POST | `/complaints/{id}/status` | Update status | No | Yes | Yes |
| POST | `/complaints/{id}/assign` | Assign technician | No | No | Yes |
| POST | `/complaints/{id}/reopen` | Reopen resolved | No | No | Yes |

### 4.4 Search Endpoints

| Method | Endpoint | Parameters | Description |
|--------|----------|------------|-------------|
| GET | `/complaints/search` | `id`, `ward`, `category`, `status`, `priority`, `page`, `size` | Multi-criteria search |

### 4.5 Dashboard Endpoint

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/dashboard` | All summary metrics from database |

### 4.6 Actuator Health Endpoint

| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/actuator/health` | Spring Boot health check |
| GET | `/actuator/info` | Application info (build, version) |

## 5. Error Handling

### 5.1 Global Exception Handler

| Exception Type | HTTP Status | Response |
|----------------|-------------|----------|
| `ConstraintViolationException` | 400 | Validation error details |
| `EntityNotFoundException` | 404 | "Resource not found" message |
| `IllegalArgumentException` | 400 | Business logic error message |
| `AccessDeniedException` | 403 | "Insufficient permissions" message |
| General `Exception` | 500 | "An unexpected error occurred" (logged) |

### 5.2 Validation Error Format

```json
{
  "status": 400,
  "errors": [
    {"field": "streetName", "message": "Street name is required"},
    {"field": "ward", "message": "Ward must not be empty"},
    {"field": "priority", "message": "Priority must be LOW, MEDIUM, HIGH, or EMERGENCY"}
  ],
  "timestamp": "2026-01-15T10:30:00Z",
  "message": "Validation failed - please correct errors and resubmit"
}
```

---