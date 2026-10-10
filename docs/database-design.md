# Database Design

## 1. Overview

The database uses MySQL 8 as the primary data store with Hibernate ORJ and Spring Data JPA for object-relational mapping. Flyway is used for versioned schema migrations. An H2 in-memory database is configured for local unit testing.

## 2. Entity Relationship Diagram (Mermaid)

```mermaid
erDiagram
    USER ||--o{ COMPLAINT : "submits"
    USER ||--o{ COMPLAINT : "assigned"
    USER ||--o{ STATUS_HISTORY : "records"
    COMPLAINT ||--o{ STATUS_HISTORY : "has"
    
    USER {
        BIGINT id PK
        VARCHAR username "unique"
        VARCHAR password "BCrypt hashed"
        ENUM role
        TIMESTAMP created_at
        TIMESTAMP updated_at
    }
    
    COMPLAINT {
        BIGINT id PK
        VARCHAR complaintId "unique, format SL-YEAR-SEQ"
        BIGINT reporter_id FK → User
        BIGINT assigned_to FK → User (nullable)
        VARCHAR street_name "NOT NULL"
        VARCHAR ward "NOT NULL"
        ENUM fault_category
        TEXT description "NOT NULL"
        ENUM priority
        ENUM status "default REPORTED"
        TIMESTAMP resolved_at "nullable"
        TEXT resolution_notes "nullable"
        TIMESTAMP created_at
        TIMESTAMP last_updated
    }
    
    STATUS_HISTORY {
        BIGINT id PK
        BIGINT complaint_id FK → Complaint
        ENUM previous_status "NOT NULL"
        ENUM new_status "NOT NULL"
        TIMESTAMP changed_at "default now"
        BIGINT changed_by FK → User "NOT NULL"
    }
```

## 3. Table Details

### 3.1 users Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AI, Not Null | Primary key |
| username | VARCHAR(50) | UK, NN, Unique | Login identifier; unique across system |
| password | VARCHAR(128) | NN | BCrypt hash; never plain text |
| role | ENUM('CITIZEN','TECHNICIAN','ADMIN') | NN, Default: CITIZEN | User role; determines authorization |
| created_at | TIMESTAMP | NN, Default: CURRENT_TIMESTAMP | Record creation time |
| updated_at | TIMESTAMP | NN, Default: CURRENT_TIMESTAMP on update | Last modification time |

**Indexes**:
- `UNIQUE INDEX idx_username (username)` — enforces unique usernames
- `INDEX idx_role (role)` — speeds up role-based queries

### 3.2 complaints Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AI, Not Null | Primary key |
| complaint_id | VARCHAR(20) | UK, NN, Unique | Generated format: SL-YEAR-SEQ (e.g., SL-2026-001) |
| reporter_id | BIGINT | NN, FK → users(id) ON DELETE RESTRICT | User who submitted the complaint |
| assigned_to | BIGINT | FK → users(id) ON DELETE SET NULL | Technician assigned; null if unassigned |
| street_name | VARCHAR(100) | NN | Street or road where streetlight is located |
| ward | VARCHAR(50) | NN | Administrative ward/area |
| fault_category | ENUM('NOT_WORKING','FLICKERING','DAMAGED_POLE','EXPOSED_WIRING','OTHER') | NN | Category of fault |
| description | TEXT | NN | Detailed description of the fault |
| priority | ENUM('LOW','MEDIUM','HIGH','EMERGENCY') | NN | Complaint priority level |
| status | ENUM('REPORTED','ACKNOWLEDGED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') | NN, Default: REPORTED | Current workflow status |
| resolved_at | TIMESTAMP | Null | Timestamp when marked RESOLVED; null if not resolved |
| resolution_notes | TEXT | Null | Notes from technician/admin who resolved |
| created_at | TIMESTAMP | NN, Default: CURRENT_TIMESTAMP | When complaint was originally submitted |
| last_updated | TIMESTAMP | NN, Default: CURRENT_TIMESTAMP on update | When status or fields last changed |

**Indexes**:
- `INDEX idx_status (status)` — frequent filtering by status
- `INDEX idx_priority (priority)` — frequent filtering by priority
- `INDEX idx_ward (ward)` — frequent filtering by location
- `INDEX idx_complaint_id (complaint_id)` — search by unique complaint ID
- `INDEX idx_reporter (reporter_id)` — citizen's complaint history
- `INDEX idx_assigned (assigned_to)` — technician's assigned complaints
- `INDEX idx_status_priority (status, priority)` — composite for dashboard queries
- `FK constraint: reporter_id → users(id)` — enforce reporter existence
- `FK constraint: assigned_to → users(id) ON DELETE SET NULL` — technician departure handling

### 3.3 status_history Table

| Column | Type | Constraints | Description |
|--------|------|-------------|-------------|
| id | BIGINT | PK, AI, Not Null | Primary key |
| complaint_id | BIGINT | NN, FK → complaints(id) ON DELETE CASCADE | The complaint this transition belongs to |
| previous_status | ENUM('REPORTED','ACKNOWLEDGED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') | NN | Status before the transition |
| new_status | ENUM('REPORTED','ACKNOWLEDGED','ASSIGNED','IN_PROGRESS','RESOLVED','CLOSED') | NN | Status after the transition |
| changed_at | TIMESTAMP | NN, Default: CURRENT_TIMESTAMP | When the change occurred |
| changed_by | BIGINT | NN, FK → users(id) ON DELETE RESTRICT | User who made the status change |

**Indexes**:
- `INDEX idx_complaint (complaint_id)` — all history for a complaint
- `INDEX idx_changed_by (changed_by)` — history by specific user
- `INDEX idx_timestamp (changed_at)` — chronological ordering
- `INDEX idx_status_previous (previous_status)` — find all complaints in prior state
- `INDEX idx_status_new (new_status)` — find all complaints in new state
- `FK constraint: complaint_id → complaints(id) ON DELETE CASCADE` — history deleted with complaint
- `FK constraint: changed_by → users(id) ON DELETE RESTRICT` — user deletion safety

## 4. Flyway Migration Management

### 4.1 Migration Strategy

- **Flyway** used for versioned schema management
- Migration files stored in `src/main/resources/db/migration/`
- Files named `V1__initial_schema.sql`, `V2__add_...sql`, etc.
- `baseline-on-migrate: true` for existing databases
- `baseline-version: "0"` for initial setup

### 4.2 Migration File Naming Convention

| Prefix | Meaning |
|--------|---------|
| `V1__` | Initial schema (create all tables) |
| `V2__` | Add new table or column |
| `V3__` | Modify existing table |
| `__` | Double underscore separates version from description |
| `.sql` | SQL file extension |

### 4.3 Example Migration Files

**V1__initial_schema.sql** (already created above, will be in repo):

**V2__add_indexes_and_constraints.sql** (future):
```sql
-- Add composite index for dashboard queries
CREATE INDEX idx_dashboard_summary ON complaints (status, priority, ward);

-- Add index for frequently searched fields
CREATE INDEX idx_search_optimized ON complaints (ward, fault_category, status);
```

**V3__add_technician_notes.sql** (future):
```sql
-- Add column for technician notes (if not in resolution_notes)
ALTER TABLE complaints ADD COLUMN technician_notes TEXT NULL AFTER resolution_notes;
```

### 4.4 Flyway Configuration (`application.yml`)

```yaml
spring:
  flyway:
    enabled: true
    baseline-on-migrate: true
    baseline-version: "0"
    locations: classpath:db/migration
    schema: streetlight
    tables: users, complaints, status_history
    validate-on-migrate: true
    fill-baseline-gaps: true
    skip-default-callback: false
```

### 4.5 H2 Test Configuration

```yaml
spring:
  datasource:
    url: jdbc:h2:mem:testdb
    username: sa
    password: ''
  jpa:
    database: h2
    show-sql: true
    hibernate:
      ddl-auto: create-drop  -- create schema for tests, drop after
```

## 5. Database Constraints and Triggers

### 5.1 Not Null Constraints

| Entity | Field | Reason |
|--------|-------|--------|
| User | username | Identifier required |
| User | password | Authentication required |
| User | role | Authorization requires role |
| Complaint | complaintId | Unique identifier |
| Complaint | streetName | Location required |
| Complaint | ward | Location required |
| Complaint | faultCategory | Classification required |
| Complaint | description | Content required |
| Complaint | priority | Priority level required |
| Complaint | status | Workflow requires status |
| StatusHistory | previousStatus | Audit trail requires prior state |
| StatusHistory | newStatus | Audit trail requires new state |
| StatusHistory | changedBy | Accountability requires user |

### 5.2 Unique Constraints

| Table | Column | Reason |
|-------|--------|--------|
| users | username | No duplicate accounts |
| complaints | complaintId | Each complaint uniquely identifiable |

### 5.3 Check Constraints (ENUM validation)

```sql
-- Fault category must be one of valid values
CHECK (fault_category IN ('NOT_WORKING', 'FLICKERING', 'DAMAGED_POLE', 'EXPOSED_WIRING', 'OTHER'));

-- Priority must be valid level
CHECK (priority IN ('LOW', 'MEDIUM', 'HIGH', 'EMERGENCY'));

-- Status must be valid workflow state
CHECK (status IN ('REPORTED', 'ACKNOWLEDGED', 'ASSIGNED', 'IN_PROGRESS', 'RESOLVED', 'CLOSED'));

-- Role must be valid user role
CHECK (role IN ('CITIZEN', 'TECHNICIAN', 'ADMIN'));
```

### 5.4 Auto-Timestamps

- **MySQL Trigger**: `created_at` and `updated_at` auto-populated on INSERT/UPDATE on `complaints` table
- **Hibernate**: `createdAt`, `lastUpdated` annotated with `@CreationTimestamp`, `@UpdateTimestamp`
- **StatusHistory**: `changed_at` defaults to `CURRENT_TIMESTAMP`

### 5.5 Indexes for Performance

| Purpose | Index |
|---------|-------|
| Status filtering | `INDEX idx_status (status)` |
| Priority filtering | `INDEX idx_priority (priority)` |
| Ward/area filtering | `INDEX idx_ward (ward)` |
| Complaint ID search | `INDEX idx_complaint_id (complaint_id)` |
| Reporter history | `INDEX idx_reporter (reporter_id)` |
| Technician assignments | `INDEX idx_assigned (assigned_to)` |
| Dashboard composite | `INDEX idx_status_priority_ward (status, priority, ward)` |
| Status history lookup | `INDEX idx_complaint (complaint_id)` |

## 6. Query Examples

### 6.1 Find All Open Complaints (for dashboard)

```sql
SELECT COUNT(*) AS open_count FROM complaints 
WHERE status != 'CLOSED';
```

### 6.2 Find Assigned Complaints

```sql
SELECT COUNT(*) AS assigned_count FROM complaints 
WHERE assigned_to IS NOT NULL;
```

### 6.3 Find In-Progress Complaints

```sql
SELECT COUNT(*) AS in_progress_count FROM complaints 
WHERE status = 'IN_PROGRESS';
```

### 6.4 Find Resolved Complaints

```sql
SELECT COUNT(*) AS resolved_count FROM complaints 
WHERE status = 'RESOLVED';
```

### 6.5 Find High-Priority + Emergency

```sql
SELECT COUNT(*) AS high_priority_count FROM complaints 
WHERE priority IN ('HIGH', 'EMERGENCY');
```

### 6.6 Recent Complaints (last 5)

```sql
SELECT * FROM complaints 
ORDER BY created_at DESC 
LIMIT 5;
```

### 6.7 Resolution Rate

```sql
SELECT 
  COUNT(CASE WHEN status = 'RESOLVED' THEN 1 END) * 100.0 / COUNT(*) AS resolution_rate
FROM complaints;
```

### 6.8 Search by Multiple Criteria

```sql
SELECT * FROM complaints 
WHERE ward = :ward 
  AND fault_category = :category 
  AND status = :status 
  AND priority = :priority
ORDER BY created_at DESC
LIMIT :pageSize OFFSET :offset;
```

### 6.9 Complaint with Status History

```sql
SELECT c.complaint_id, c.street_name, c.priority, c.status,
       sh.previous_status, sh.new_status, sh.changed_at, u.username AS changed_by
FROM complaints c
LEFT JOIN status_history sh ON c.id = sh.complaint_id
LEFT JOIN users u ON sh.changed_by = u.id
WHERE c.id = :complaintId
ORDER BY sh.changed_at DESC;
```

## 7. Database Setup Script

### 7.1 Initial Database Creation

```sql
-- Create the database
CREATE DATABASE streetlight CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Create the user (run after Docker MySQL starts)
CREATE USER IF NOT EXISTS 'streetlight'@'localhost' IDENTIFIED BY 'your_password_here';
GRANT ALL PRIVILEGES ON streetlight.* TO 'streetlight'@'localhost';
FLUSH PRIVILEGES;
```

### 7.2 Flyway Initialization

```powershell
# First run - baseline and migrate
.\mvnw flyway:migrate

# Subsequent runs - only new migrations apply
.\mvnw flyway:migrate
```

### 7.3 Verifying Schema

```powershell
# List all migrations
.\mvnw flyway:info

# Repair flyway schema if needed
.\mvnw flyway:repair
```

---