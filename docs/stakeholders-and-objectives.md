# Stakeholders and Objectives

## Stakeholder Analysis

### 1. Citizens (Primary Users)
- **Role**: Report streetlight faults, track complaint status
- **Actions**: 
  - Submit new complaint with streetlight details, fault category, description, priority
  - View list of their own complaints
  - View details of individual complaints
  - Track status changes from REPORTED → CLOSED
- **Success Metrics**: 
  - Complaint submission time < 5 minutes
  - Can view complaint status within 1 second of update
  - Intuitive form with clear validation errors

### 2. Technicians (Secondary Users)
- **Role**: Repair assigned streetlights, update progress
- **Actions**:
  - View complaints assigned to them or accessible by ward
  - Update complaint status: IN_PROGRESS → RESOLVED
  - Add resolution notes and completion timestamp
  - Cannot change status from RESOLVED → REPORTED without admin approval (workflow enforcement)
- **Success Metrics**:
  - Assigned complaints visible within 2 seconds of assignment
  - Status update immediately reflected in citizen's view
  - Resolution notes properly recorded and visible

### 3. Admins (Power Users)
- **Role**: Manage complaints, assign technicians, control workflow, view dashboard
- **Actions**:
  - View all complaints regardless of assignment
  - Assign technicians to complaints
  - Change complaint status following valid workflow transitions
  - Reopen resolved complaints if required
  - Manage user accounts (create initial admin account)
  - View summary dashboard with all metrics
  - Generate reports on resolution rates, response times
- **Success Metrics**:
  - Can assign technician to any complaint
  - Status workflow enforced (invalid transitions blocked)
  - Dashboard values match database counts within 1%
  - Can reopen any resolved complaint

### 4. Municipality (Organization)
- **Role**: Oversee the system, track metrics, improve services
- **Actions**:
  - Monitor resolution rates and response times
  - Identify high-fault areas by ward or street
  - Track high-priority and emergency complaint response
  - Evaluate technician performance and workload balance
- **Success Metrics**:
  - Dashboard provides accurate total/open/resolved counts
  - High-priority complaints highlighted and responded to within SLA
  - Resolution rate metric calculated from database
  - Complaint counts by status and priority group properly

## Objectives Summary

| Objective | Description | Measurement |
|-----------|-------------|-------------|
| **O1**: Citizen complaint submission | Citizens can submit streetlight fault reports via web form with validation | Form submits successfully with valid data; validation errors shown for invalid data |
| **O2**: Role-based authentication | Three distinct roles (Citizen, Technician, Admin) with proper access control | Login works for all roles; unauthorized access to role-restricted pages blocked |
| **O3**: Status workflow management | Controlled workflow: REPORTED → ACKNOWLEDGED → ASSIGNED → IN_PROGRESS → RESOLVED → CLOSED | Server enforces valid transitions; invalid transitions rejected |
| **O4**: Complete complaint history | Every status change recorded with previous status, new status, timestamp, user | Status history table populated for each transition |
| **O5**: Search and filtering | Search by complaint ID, location, ward, category, status, priority | Search returns relevant results; pagination works correctly |
| **O6**: Summary dashboard | Real-time dashboard with key metrics from database | Total, open, assigned, in-progress, resolved, high-priority, recent complaints displayed |
| **O7**: Docker deployment | Application runs in Docker with MySQL and Nginx reverse proxy | `docker compose up` brings up all services; health checks pass |
| **O8**: CI/CD pipeline | Jenkins builds, tests, packages, deploys automatically | Pipeline runs on commit; failed tests stop deployment |
| **O9**: Ansible provisioning | Infrastructure as code for deployment | Playbook configures target; idempotent on second run |
| **O10**: Viva readiness | Student can explain complete system | Student presents architecture, data flow, DevOps pipeline, security |

## Success Criteria Matrix

| # | Success Criterion | Test Method | Pass Condition |
|---|-------------------|-------------|----------------|
| 1 | Citizen can submit complaint | Form validation + API test | Submission succeeds with valid data; errors shown for invalid |
| 2 | Technician can update status | API + UI test | Status changes to valid next state; invalid blocked |
| 3 | Admin can assign technician | API + UI test | Assignment succeeds; other roles cannot |
| 4 | Dashboard shows accurate counts | Query database vs UI display | All values match database records |
| 5 | Search by category works | Search API test | Returns complaints matching category filter |
| 6 | Passwords are hashed | Code review | Password column uses BCryptPasswordEncoder; not plain text |
| 7 | Docker container runs | `docker compose up` | All services start; app connects to MySQL |
| 8 | Jenkins pipeline completes | Jenkins build log | All stages pass; failed tests block deployment |
| 9 | Ansible idempotency | `ansible-playbook` run twice | Second run: `--changed=0` or only idempotent changes |
| 10 | Student can explain system | Viva interview | Student describes architecture, flow, DevOps, security accurately |

## Development Bootstrap Process

### Initial Administrator Account Creation

Since the system requires an initial admin account, a safe bootstrap process is required:

1. **Default Admin Credentials (Development Only)**:
   - Username: `admin`
   - Password: `admin123` (temporary, changed after first login)
   - Role: `ADMIN`

2. **Bootstrap Mechanism**:
   - On first startup with empty database, system auto-creates admin account
   - Admin must change password on first login
   - Default credentials are **not** committed to source code
   - `.env.example` provides placeholder values, not real credentials

3. **Safe Initialization Code (conceptual)**:
   ```java
   // In a CommandLineRunner or ApplicationListener
   if (userRepository.count() == 0) {
       User admin = new User();
       admin.setUsername("admin");
       admin.setPassword(passwordEncoder.encode("admin123"));
       admin.setRole(Role.ADMIN);
       userRepository.save(admin);
   }
   ```
   
4. **Never Commit**: Default passwords, database credentials, or secrets to GitHub
5. **`.env.example`** contains only placeholder values like `DB_PASSWORD=your_password_here`

### Password Hashing Strategy
- **Algorithm**: BCrypt with strength factor 10
- **Implementation**: Spring Security's `BCryptPasswordEncoder`
- **Storage**: `password` column in `users` table stores hashed password only
- **Verification**: `PasswordEncoder.matches()` during login

### Development Workflow Bootstrap
```powershell
# 1. Clone repository
git clone <repo-url>
cd streetlight-fault-reporting

# 2. Verify Maven Wrapper
./mvnw --version

# 3. Set up environment variables
# Create .env file from .env.example
copy .env.example .env
# Edit .env with actual values (not committed)

# 4. Initialize database
docker compose up -d mysql

# 5. Run application
./mvnw spring-boot:run

# 6. Access application
# http://localhost:8080 (or nginx reverse proxy URL)
```

### Documentation Commitment
- All docs committed to repository
- `.gitignore` excludes `.env`, `target/`, `.m2/`, Docker credentials
- `README.md` includes setup instructions and success criteria overview