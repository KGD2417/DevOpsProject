# Use Cases

## 1. Primary Use Cases (UC-1 to UC-10)

### UC-1: Citizen Registration

**Actor**: Citizen (new user)  
**Preconditions**: User navigates to registration page; no existing account  
**Postconditions**: Citizen account created with CITIZEN role; user automatically logged in  

#### Basic Flow:
1. User clicks "Register" from the login/index page
2. Registration form displayed with fields: username, password, confirm password
3. User enters desired username and password (meets complexity requirements)
4. User clicks "Register" button
5. System validates: username unique, password strength, password match
6. If validation fails: error messages displayed; user corrects and resubmits
7. If validation succeeds: new User entity created with BCrypt-hashed password and CITIZEN role
8. Account saved to database
9. Session created; user redirected to dashboard
10. Success message: "Registration successful. Your username is: [username]"

#### Alternative Flows:
- **Username already exists**: Error "Username already taken. Please choose another." displayed
- **Password too weak**: Error "Password must be at least 6 characters" displayed
- **Passwords don't match**: Error "Passwords do not match. Please try again." displayed

### UC-2: Citizen Login

**Actor**: Citizen with existing account  
**Preconditions**: No active session; user on login page  
**Postconditions**: Authenticated session; user redirected to dashboard  

#### Basic Flow:
1. User navigates to `/login` or clicks "Login" link
2. Login form displayed with fields: username, password
3. User enters credentials
4. User clicks "Login" button
5. System authenticates: checks username exists, compares BCrypt hash
6. If authentication fails: error "Bad credentials" displayed; user re-enters
7. If authentication succeeds: session created with user's authorities (role)
8. Last login timestamp updated (optional)
9. User redirected to dashboard (or intended page before login)
10. Success: user sees dashboard with role-appropriate content

#### Alternative Flows:
- **Remember me** (future): persistent cookie for auto-login on next visit
- **Caps lock detected**: Warning "Caps lock is on" shown

### UC-3: Citizen Submit Complaint

**Actor**: Citizen (authenticated)  
**Preconditions**: User logged in; on complaint creation page  
**Postconditions**: New complaint with status REPORTED; associated with reporter  

#### Basic Flow:
1. User navigates to `/complaints/new` or clicks "Report Fault" CTA
2. Complaint creation form displayed with required fields
3. User fills in: street name, ward, fault category, description, priority
4. (Optional) User provides streetlight identifier if known
5. User clicks "Submit Complaint" button
6. System validates all required fields present and valid
7. If validation fails: error messages displayed next to invalid fields; user corrects
8. If validation succeeds:
   - Unique complaintId generated (format: SL-YEAR-SEQ, e.g., SL-2026-001)
   - Status defaults to REPORTED
   - Reporter set to authenticated user
   - Complaint saved to database
   - Status history entry created: REPORTED (initial state)
   - User redirected to complaint detail page
   - Success message: "Complaint submitted successfully. Complaint ID: SL-2026-001"

#### Alternative Flows:
- **Missing required field**: Error "Street name is required" displayed near field
- **Invalid fault category**: Error "Please select a valid fault category" displayed
- **Invalid priority**: Error "Please select a valid priority level" displayed
- **Database error**: Error "Failed to save complaint. Please try again." displayed

### UC-4: Citizen View Own Complaints

**Actor**: Citizen (authenticated)  
**Preconditions**: User logged in; on complaints listing page  
**Postconditions**: List of complaints filtered by authenticated user  

#### Basic Flow:
1. User navigates to `/complaints` (or clicks "My Complaints" CTA)
2. System retrieves complaints where reporter_id = authenticated user's user ID
3. Pagination applied (default: 10 per page)
4. Complaints list displayed with: complaint ID, street name, priority, status, date submitted, action links
5. If no complaints exist: empty state message "You have not submitted any complaints yet."
6. User can click on any complaint to view detail page

#### UI Elements:
- Table columns: #, Complaint ID, Street, Ward, Priority, Status, Reported On, Actions
- Pagination controls at bottom
- "Submit New Complaint" button (if within role permissions)

### UC-5: Technician View Assigned Complaints

**Actor**: Technician (authenticated)  
**Preconditions**: User logged in with TECHNICIAN role  
**Postconditions**: List of complaints assigned to technician or accessible by ward  

#### Basic Flow:
1. User logs in with TECHNICIAN credentials
2. User navigates to `/complaints` (technician view)
3. System retrieves complaints where assigned_to = authenticated user's user ID
4. Additionally, show complaints where ward matches technician's accessible ward (if applicable)
5. Pagination applied
6. List displayed with complaint details and current status
7. Status update controls visible for complaints in valid transition states

#### Additional Technician View:
- Complaints assigned to them (assigned_to = their user ID)
- Complaints in their ward (if they have ward-level assignment)
- Current status badge showing REPORTED/ACKNOWLEDGED/ASSIGNED/IN_PROGRESS/RESOLVED/CLOSED
- Status update button available for: REPORTED→ACKNOWLEDGED, ASSIGNED→IN_PROGRESS, IN_PROGRESS→RESOLVED
- "Mark as Resolved" button available when status = IN_PROGRESS

### UC-6: Technician Update Complaint Status

**Actor**: Technician (authenticated, assigned complaint)  
**Preconditions**: User on complaint detail page; complaint status allows transition  
**Postconditions**: Status updated; history recorded; resolution notes if RESOLVED  

#### Basic Flow:
1. Technician navigates to complaint detail page
2. Status update section visible (if user has permission and valid transition)
3. Technician selects new status from valid options (depends on current status)
4. If transitioning to RESOLVED: technician enters resolution notes (required)
5. Technician clicks "Update Status" button
6. System validates: current status matches expected previous status for this transition
7. If invalid transition: error "Invalid status transition from [current] to [selected]" displayed
8. If resolution notes missing for RESOLVED: error "Resolution notes required for RESOLVED status" displayed
9. If valid:
   - Complaint status updated to new status
   - New entry created in status_history table:
     - previousStatus = old status
     - newStatus = new status
     - changedAt = current timestamp
     - changedBy = authenticated technician
   - If newStatus = RESOLVED: resolved_at timestamp set, resolution_notes saved
   - Complaint detail page refreshes with new status
   - Success message: "Status updated to IN_PROGRESS" / "Status updated to RESOLVED"
   - Dashboard metrics refresh (if visible to user)

#### Valid Transition Matrix (enforced server-side):

| Current Status | Valid Next Status | Notes |
|----------------|-------------------|-------|
| REPORTED | ACKNOWLEDGED | Acknowledgment by system |
| REPORTED | ASSIGNED | Direct assignment (admin can skip ACKNOWLEDGED) |
| ACKNOWLEDGED | ASSIGNED | After acknowledgment |
| ASSIGNED | IN_PROGRESS | Technician starts work |
| IN_PROGRESS | RESOLVED | Requires resolution notes |
| RESOLVED | CLOSED | Admin only; after verification |
| RESOLVED | IN_PROGRESS | Admin reopens complaint |

**Invalid Transitions** (blocked, error shown):
- Any transition not in the valid matrix above
- IN_PROGRESS → REPORTED (rollback not allowed via this UI)
- CLOSED → any other state (must reopen first via admin)

### UC-7: Admin Manage Complaints

**Actor**: Admin (authenticated)  
**Preconditions**: User logged in with ADMIN role  
**Postconditions**: Full access to all complaint management functions  

#### Basic Flow:
1. Admin logs in with ADMIN credentials
2. Admin navigates to `/complaints` (admin view)
3. System displays ALL complaints regardless of assignment or reporter
4. Admin can view detail page for any complaint
5. Admin has access to additional action buttons:
   - "Assign Technician" — assign tech to complaint
   - "Reopen Complaint" — reopen RESOLVED complaint to IN_PROGRESS
   - "Delete Complaint" — permanently delete (with confirmation)
   - "Change Status" — update status through valid workflow

#### Admin-Specific Actions:

**Assign Technician**:
1. Admin selects technician from dropdown (list of all users with TECHNICIAN role)
2. Clicks "Assign" button
3. Complaint's assigned_to field set to selected technician
4. Success message: "Technician [name] assigned to complaint [ID]"

**Reopen Resolved Complaint**:
1. Admin navigates to resolved complaint
2. Clicks "Reopen" button
3. Complaint status changes from RESOLVED to IN_PROGRESS
4. Status history recorded: previous=RESOLVED, new=IN_PROGRESS, by admin
5. Success message: "Complaint [ID] reopened to IN_PROGRESS"

**Delete Complaint**:
1. Admin views complaint detail page
2. Clicks "Delete" (with confirmation dialog: "Are you sure you want to delete this complaint?")
3. Complaint permanently removed from database
4. Associated status_history entries also removed (CASCADE)
5. Success message: "Complaint [ID] deleted"
6. If complaint has attachments or related data: warning shown

**Change Status**:
1. Admin selects new valid status from dropdown
2. (Optional) Adds resolution notes if transitioning to RESOLVED
3. Clicks "Update Status"
4. Status updated with full audit trail
5. Success message: "Status updated to [new status]"

### UC-8: User Search Complaints

**Actor**: Any authenticated user (Citizen/Technician/Admin)  
**Preconditions**: User on search page or search form visible  
**Postconditions**: Filtered list of complaints matching criteria  

#### Basic Flow:
1. User navigates to `/complaints/search` or clicks "Search" CTA
2. Search form displayed with optional fields:
   - Complaint ID
   - Street name/location (partial match)
   - Ward/area
   - Fault category
   - Status
   - Priority
   - Date range (reported from/to)
3. User enters one or more search criteria
4. User clicks "Search" button
5. System queries database with all provided filters
6. Results displayed with pagination
7. If no matches: empty state "No complaints match the search criteria."
8. User can refine search or clear filters

#### Searchable Fields and Behavior:

| Field | Behavior |
|-------|----------|
| Complaint ID | Exact match; returns single complaint or none |
| Street Name | Partial match (LIKE %streetName%); case-insensitive |
| Ward | Exact match on ward column |
| Fault Category | Exact match on ENUM value |
| Status | Exact match on workflow status |
| Priority | Exact match on priority ENUM |
| Date Range | Complaints reported between from and to dates |
| Combined Search | All provided criteria combined with AND |

#### UI Features:
- Clear all filters button
- Pagination on results
- "Reset to all complaints" link
- Summary of applied filters shown above results

### UC-9: View Summary Dashboard

**Actor**: Any authenticated user  
**Preconditions**: User logged in; on dashboard page  
**Postcards**: Summary metrics computed from actual database records  

#### Basic Flow:
1. User navigates to `/dashboard` (or dashboard is default landing page)
2. Page loads with responsive grid of metric cards
3. Each card fetches count from database via repository methods
4. Cards displayed with: value, label, optional trend indicator
5. Recent complaints section shows last 5 submissions
6. Empty state shown when database has no records

#### Dashboard Card Details:

| Card | Value Calculation | Visual |
|------|-------------------|--------|
| **Total Complaints** | `SELECT COUNT(*) FROM complaints` | Large number, grey card |
| **Open Complaints** | `SELECT COUNT(*) FROM complaints WHERE status != 'CLOSED'` | Number, yellow/orange accent |
| **Assigned Complaints** | `SELECT COUNT(*) FROM complaints WHERE assigned_to IS NOT NULL` | Number, blue accent |
| **In Progress** | `SELECT COUNT(*) FROM complaints WHERE status = 'IN_PROGRESS'` | Number, progress bar indicator |
| **Resolved** | `SELECT COUNT(*) FROM complaints WHERE status = 'RESOLVED'` | Number, green accent |
| **High-Priority + Emergency** | `SELECT COUNT(*) FROM complaints WHERE priority IN ('HIGH','EMERGENCY')` | Number, red accent |
| **Recent Complaints** | `SELECT * FROM complaints ORDER BY created_at DESC LIMIT 5` | List with: ID, street, priority, status, date |
| **Resolution Rate** | `SELECT COUNT(CASE WHEN status='RESOLVED' THEN 1 END)*100.0/COUNT(*) FROM complaints` | Percentage, displayed as "Resolution Rate: 67%" |

#### Empty State (no complaints in system):
- All cards show "0" or "N/A"
- Recent complaints section: "No complaints submitted yet. Be the first to report a fault!"
- Resolution rate: "N/A (no complaints)"
- Visual: subtle placeholder illustration or text

### UC-10: Unauthorized Access Restriction

**Actor**: Any user attempting to access role-restricted functionality  
**Preconditions**: User not authenticated or user lacks required role  
**Postconditions**: Access denied; user redirected to appropriate page  

#### Basic Flow:
1. User (Citizen) attempts to access admin-only URL: `/complaints/{id}/assign`
2. Spring Security intercepts request
3. User not authenticated → redirect to `/login` with intended URL recorded
4. User authenticated as CITIZEN but lacks ADMIN role → 403 Forbidden or redirect to access-denied page
5. Technician attempts admin-only endpoint: `/complaints/{id}/reopen`
6. Same: 403 Forbidden or access-denied redirect
7. Admin attempts technician-restricted endpoint: should have access (by role)

#### Alternative Flows:

**403 Forbidden (Spring Security default)**:
- Response: HTTP 403 with Spring Security error page
- User sees: "Access Denied - Insufficient permissions"
- Logged in session remains active

**Redirect to Login**:
- Unauthenticated user redirected to `/login?redirect=/complaints/{id}`
- After login, user redirected to appropriate page based on their role
- If Citizen logs in: redirected to dashboard (not complaint management)

**Custom Access-Denied Page**:
- `/access-denied` Thymeleaf template
- Displays: "You do not have permission to access this page."
- Provides link back to dashboard or login
- logged-in users see role-appropriate message

#### Authorization Enforcement Points:

| Access Point | Required Role | Enforcement |
|--------------|---------------|-------------|
| `/complaints/new` | Any (CITIZEN+) | `sec:authorize` or controller method security |
| `/complaints/{id}/status` | TECHNICIAN or ADMIN | Method-level `@PreAuthorize("hasRole('TECHNICIAN') or hasRole('ADMIN')")` |
| `/complaints/{id}/assign` | ADMIN only | Method-level `@PreAuthorize("hasRole('ADMIN')")` |
| `/complaints/{id}/reopen` | ADMIN only | Method-level `@PreAuthorize("hasRole('ADMIN')")` |
| `/complaints/search` | Any (with filters) | Anonymous access allowed; role restricts some filter results |
| `/dashboard` | Any authenticated | `isAuthenticated()` check |
| `/complaints/{id}` | Any authenticated | Viewable by any role; actions role-dependent |
| `/actuator/health` | None (health) | No security; monitoring endpoint |

#### Frontend + Backend Defense:
- **Frontend**: Thymeleaf `sec:authorize` hides/shows buttons/links based on role
- **Backend**: Spring Security `@PreAuthorize` blocks HTTP access regardless of frontend
- **Never rely only on hiding buttons**: server-side authorization always enforced
- **All sensitive endpoints** have `@PreAuthorize` or `@RolesAllowed` annotations

---