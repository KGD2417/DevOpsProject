# Minimum Viable Product (MVP) Scope

## MVP Feature Set

The MVP (Minimum Viable Product) for the Automated Streetlight Fault Reporting System includes the core features necessary for a functional demonstration suitable for college project viva. The scope is limited to essential functionality that demonstrates all required roles, workflows, and DevOps practices.

### Core Features (Must-Have)

| Feature | Description | User Role(s) |
|---------|-------------|------------|
| **Authentication & Authorization** | Secure login/logout with role-based access (Citizen, Technician, Admin) | All |
| **Complaint CRUD** | Create, view, update permitted fields, delete (admin only) | Citizen, Technician, Admin |
| **Status Workflow** | Controlled transitions: REPORTED → ACKNOWLEDGED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED | Technician, Admin |
| **Complaint Search** | Search by ID, location, ward, category, status, priority | All |
| **Pagination** | Paginated complaint listings | All |
| **Dashboard** | Summary cards from database records | All |
| **Status History** | Timestamped history of all status changes with user | All |
| **Validation** | Server-side validation with useful error messages | All |

### Role-Specific Features

#### Citizen Features
- Submit new complaint with all required fields
- View own complaints (filtered by authenticated user)
- View complaint details including status history
- Cannot update complaints or change status

#### Technician Features
- View assigned or ward-accessible complaints
- Update status following valid workflow transitions
- Add resolution notes when marking RESOLVED
- Cannot create new complaints or change assigned technician without admin

#### Admin Features
- Manage all complaints (view, update, delete)
- Assign technicians to complaints
- Change complaint status through workflow
- Reopen resolved complaints when needed
- Manage user accounts (create initial admin)
- View complete dashboard with all metrics

### Out-of-Scope (for MVP)

| Feature | Reason for Exclusion |
|---------|---------------------|
| **Email notifications** | Adds complexity; can be added later |
| **Mobile responsive design** beyond Bootstrap 5 | Bootstrap 5 provides adequate responsiveness |
| **Advanced reporting/analytics** | Dashboard provides basic metrics; advanced reports added later |
| **File attachments** (photos of faulty streetlights) | Not core to complaint management |
| **Comments/discussion thread** on complaints | Out of MVP scope |
| **Bulk operations** | Single complaint operations suffice for demo |
| **API endpoints for external integration** | Internal web application only |
| **Multi-tenancy** | Single instance for college project |
| **Audit trail beyond status history** | Status history suffices for demonstration |

### Database Model (MVP)

#### Entity Relationships

```
User (1) → (N) Complaint
- One user can submit many complaints
- Complaint belongs to one user

Complaint (1) → (1..N) StatusHistory
- One complaint has many status history entries
- Status history belongs to one complaint

User (1) → (1) Role
- One user has one role (Citizen, Technician, Admin)
- Role has many users
```

#### Entity Attributes

**User Entity**:
- `id` (PK, generated)
- `username` (unique, not null)
- `password` (hashed, not null)
- `role` (ENUM: CITIZEN, TECHNICIAN, ADMIN)
- `created_at`, `updated_at`

**Complaint Entity**:
- `id` (PK, generated)
- `complaintId` (unique generated string, e.g., SL-2024-001)
- `streetlightId` (optional, citizen-provided)
- `streetName` (not null)
- `ward` (not null)
- `faultCategory` (ENUM: NOT_WORKING, FLICKERING, DAMAGED_POLE, EXPOSED_WIRING, OTHER)
- `description` (not null)
- `priority` (ENUM: LOW, MEDIUM, HIGH, EMERGENCY)
- `reportedAt` (timestamp, auto-created)
- `assignedTo` (foreign key to User, technician, nullable)
- `status` (ENUM: REPORTED, ACKNOWLEDGED, ASSIGNED, IN_PROGRESS, RESOLVED, CLOSED)
- `resolvedAt` (nullable, timestamp)
- `resolutionNotes` (nullable, text)
- `createdBy` (foreign key to User)
- `lastUpdated` (timestamp, auto-updated)

**StatusHistory Entity**:
- `id` (PK, generated)
- `complaint` (foreign key to Complaint)
- `previousStatus` (not null, ENUM)
- `newStatus` (not null, ENUM)
- `changedAt` (timestamp, auto-created)
- `changedBy` (foreign key to User)

#### Database Constraints
- Foreign key constraints on assignedTo, changedBy, createdBy
- NOT NULL constraints on essential fields
- Unique constraint on complaintId
- Index on status, priority, ward for search performance
- ENUM constraints on faultCategory, priority, status, role

### API Endpoints (MVP)

#### Authentication
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/login` | Login page |
| POST | `/login` | Process login form |
| GET | `/logout` | Logout and redirect |

#### Citizen Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/complaints` | List citizen's complaints (with pagination, search) |
| GET | `/complaints/new` | Show complaint creation form |
| POST | `/complaints` | Create new complaint |
| GET | `/complaints/{id}` | View complaint details |
| GET | `/complaints/{id}/history` | View status history |

#### Technician Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/complaints` | List accessible complaints (assigned + ward) |
| GET | `/complaints/{id}` | View complaint details |
| POST | `/complaints/{id}/status` | Update status (valid transition) |
| POST | `/complaints/{id}/resolve` | Mark as resolved with notes |

#### Admin Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/complaints` | List all complaints (with filters, pagination) |
| GET | `/complaints/{id}` | View complaint details |
| POST | `/complaints/{id}/status` | Update status (any valid transition) |
| POST | `/complaints/{id}/assign` | Assign technician to complaint |
| POST | `/complaints/{id}/reopen` | Reopen resolved complaint |
| GET | `/dashboard` | Summary dashboard data |

#### Search and Filter Endpoints
| Method | Endpoint | Description |
|--------|----------|-------------|
| GET | `/complaints/search` | Search by multiple criteria |
| GET | `/complaints/filter` | Filter by status, priority, ward, category |

### Thymeleaf Templates (MVP)

| Template | Description | Used By |
|----------|-------------|---------|
| `layouts/default.html` | Base layout with navigation | All pages |
| `index.html` | Homepage / landing page | All users |
| `dashboard.html` | Summary dashboard with cards | All authenticated |
| `complaints/list.html` | Paginated complaint list with search | All |
| `complaints/form.html` | Complaint creation/editing form | Citizen, Admin |
| `complaints/detail.html` | Complaint detail view with history | All |
| `auth/login.html` | Login form | All (unauthenticated) |
| `admin/` | Admin-specific views | Admin role |
| `technician/` | Technician-specific views | Technician role |

### Screenshots and Evidence

#### Required Evidence for Milestone Completion
1. **Working application screenshot**: Dashboard with complaint cards showing real data
2. **Complaint creation form**: Successfully submitted complaint with validation
3. **Search functionality**: Search results filter by category/ward/status
4. **Role-based access**: Citizen cannot see admin-only buttons/links
5. **Status transition**: Valid workflow progression visible in status history
6. **Dashboard metrics**: Values match database records (verified via MySQL client)
7. **Selenium test failure screenshots**: When tests fail
8. **Docker compose up**: All services running
9. **Jenkins pipeline log**: All stages complete successfully
10. **Ansible playbook run**: Second run shows idempotency

### Definition of Done for MVP

The MVP is complete when ALL of the following are verified:

- [ ] Application compiles with `./mvnw` (Maven Wrapper)
- [ ] Docker containers start with `docker compose up -d`
- [ ] Application accessible at `http://localhost:8080` or nginx URL
- [ ] User can register/login with three roles (Citizen, Technician, Admin)
- [ ] Citizen can submit a complaint and view it
- [ ] Technician can update status through valid workflow transitions
- [ ] Admin can assign technicians and manage complaints
- [ ] Status history is recorded for each transition
- [ ] Dashboard displays accurate counts from database
- [ ] Search by complaint ID, ward, category, status, priority works
- [ ] Pagination works on complaint listing pages
- [ ] Validation errors shown for invalid form submissions
- [ ] Unauthorized access restricted (role-based)
- [ ] Selenium tests pass (5 user journeys)
- [ ] Docker image builds and pushes to Docker Hub (documented)
- [ ] Jenkins pipeline runs end-to-end
- [ ] Ansible playbook runs idempotently
- [ ] All documentation files created and populated
- [ ] Git history shows meaningful commits across branches
- [ ] Version tag `v1.0.0` created for release-ready baseline

### MVP Success Threshold

**Minimum threshold** (MVP considered functional):
- All core CRUD operations work for all three roles
- Authentication and authorization enforced server-side
- Status workflow transitions are validated
- Dashboard values computed from database
- Search and filter functionality works
- Maven build and Docker deployment work
- At least 3 of 5 Selenium tests pass

**Stretch target** (full MVP):
- All 5 Selenium tests pass
- Docker Compose with MySQL + app + Nginx
- Jenkins pipeline with all 12 stages
- Ansible idempotency verified
- Full documentation set complete

---