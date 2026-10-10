# API Reference

## 1. Overview

The Automated Streetlight Fault Reporting System provides a RESTful API via Spring MVC controllers, with HTML views rendered by Thymeleaf. The base application URL is `http://localhost:8080`. API endpoints that return data use JSON; Thymeleaf endpoints return HTML views.

## 2. Base Information

| Property | Value |
|----------|-------|
| **Base URL** | `http://localhost:8080` (Thymeleaf views) |
| **API Prefix** | `/api/complaints` (internal) |
| **Content-Type** | HTML (views), JSON (internal API) |
| **Authentication** | Form-based login (Spring Security) |
| **Response Format** | HTML views for web; JSON for AJAX/internal |

## 3. Endpoint Categories

### 3.1 Authentication Endpoints

| Method | Endpoint | Description | Auth Required | Returns |
|--------|----------|-------------|---------------|---------|
| GET | `/login` | Login form page | No | HTML login form |
| POST | `/login` | Process login credentials | No | Redirect or error |
| GET | `/logout` | Logout and invalidate session | Yes (session) | Redirect to index |

**Request Parameters (POST /login)**:
- `username` (String) — login username
- `password` (String) — plain text password (BCrypt-encoded in DB)

**Success Response**: Session created, redirect to `/`
**Error Response**: 401 Unauthorized, login page with error message "Bad credentials"

### 3.2 Complaint Endpoints

#### 3.2.1 List Complaints

| Method | Endpoint | Description | Auth | Parameters |
|--------|----------|-------------|------|------------|
| GET | `/complaints` | List complaints with pagination and filter | Yes | `page` (default 0), `size` (default 10), `ward`, `category`, `status`, `priority`, `search` |

**Query Parameters**:
- `page` — zero-based page number (default: 0)
- `size` — page size (default: 10)
- `ward` — filter by ward/area
- `category` — filter by fault category
- `status` — filter by status
- `priority` — filter by priority
- `search` — free-text search (complaintId, streetName)

**Success Response**: HTML table with paginated complaints, or JSON (if Accept: application/json)

#### 3.2.2 Create Complaint

| Method | Endpoint | Description | Auth | Request Body |
|--------|----------|-------------|------|--------------|
| GET | `/complaints/new` | Show complaint creation form | Yes (any role) | HTML form |
| POST | `/complaints` | Submit new complaint | Yes | Multipart/form-data |

**Request Body (POST /complaints)**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| streetName | String | Yes | Street or road name |
| ward | String | Yes | Administrative ward/area |
| faultCategory | Enum | Yes | NOT_WORKING, FLICKERING, DAMAGED_POLE, EXPOSED_WIRING, OTHER |
| description | String | Yes | Detailed fault description |
| priority | Enum | Yes | LOW, MEDIUM, HIGH, EMERGENCY |
| streetlightId | String | No | Optional streetlight identifier |
| reporterUserId | Long | Implicit | Auto-filled from authenticated user |

**Success Response**: Redirect to `/complaints/{complaintId}` with success message
**Validation Errors**: 400 Bad Request, form re-display with error messages

#### 3.2.3 View Complaint Detail

| Method | Endpoint | Description | Auth | Path Variables |
|--------|----------|-------------|------|----------------|
| GET | `/complaints/{id}` | View complaint detail | Yes | `{id}` = complaint database ID |

**Success Response**: HTML page with complaint info, status history, role-appropriate actions

**Role-Accessible Actions (shown on detail page)**:
- **Citizen**: View only (no action buttons)
- **Technician**: Status update button (if valid transition)
- **Admin**: Assign technician, reopen resolved, change status

#### 3.2.4 Update Status

| Method | Endpoint | Description | Auth | Path Variables |
|--------|----------|-------------|------|----------------|
| POST | `/complaints/{id}/status` | Update complaint status | Yes (TECHNICIAN/ADMIN) | `{id}` = complaint ID |

**Request Body (POST /complaints/{id}/status)**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| newStatus | Enum | Yes | Next valid status |
| resolutionNotes | String | Required if newStatus=RESOLVED | Notes for resolution |

**Success Response**: 302 Redirect to `/complaints/{id}` with success message
**Invalid Transition**: 400 Bad Request, error "Invalid status transition from X to Y"
**Resolution Notes Missing**: 400 Bad Request, "Resolution notes required for RESOLVED status"

#### 3.2.5 Assign Technician

| Method | Endpoint | Description | Auth | Path Variables |
|--------|----------|-------------|------|----------------|
| POST | `/complaints/{id}/assign` | Assign technician to complaint | Yes (ADMIN only) | `{id}` = complaint ID |

**Request Body (POST /complaints/{id}/assign)**:
| Field | Type | Required | Description |
|-------|------|----------|-------------|
| technicianUserId | Long | Yes | User ID of technician to assign |

**Success Response**: 302 Redirect to `/complaints/{id}` with "Technician assigned" message
**Non-Admin Access**: 403 Forbidden, "Insufficient permissions"

#### 3.2.6 Reopen Resolved Complaint

| Method | Endpoint | Description | Auth | Path Variables |
|--------|----------|-------------|------|----------------|
| POST | `/complaints/{id}/reopen` | Reopen resolved complaint | Yes (ADMIN only) | `{id}` = complaint ID |

**Success Response**: 302 Redirect to `/complaints/{id}` with "Complaint reopened" message
**Non-Admin Access**: 403 Forbidden

#### 3.2.7 Delete Complaint

| Method | Endpoint | Description | Auth | Path Variables |
|--------|----------|-------------|------|----------------|
| DELETE | `/complaints/{id}` | Delete complaint | Yes (ADMIN only) | `{id}` = complaint ID |

**Success Response**: 302 Redirect to `/complaints` with "Complaint deleted" message
**Non-Admin Access**: 403 Forbidden

### 3.3 Search Endpoints

| Method | Endpoint | Description | Auth | Parameters |
|--------|----------|-------------|------|------------|
| GET | `/complaints/search` | Multi-criteria search | Yes | `id`, `ward`, `category`, `status`, `priority`, `page`, `size` |

**Query Parameters**:
- `complaintId` or `id` — search by unique complaint ID
- `ward` — filter by ward/area
- `category` — filter by fault category
- `status` — filter by status
- `priority` — filter by priority
- `page` — page number (default 0)
- `size` — page size (default 10)

**Success Response**: HTML table or JSON array of matching complaints

### 3.4 Dashboard Endpoint

| Method | Endpoint | Description | Auth | Returns |
|--------|----------|-------------|------|---------|
| GET | `/dashboard` | Summary dashboard data | Yes (any authenticated) | JSON or HTML with metric cards |

**Response Metrics**:
| Metric | Description | SQL Equivalent |
|--------|-------------|----------------|
| `totalComplaints` | Total records in complaints table | `SELECT COUNT(*) FROM complaints` |
| `openComplaints` | status != 'CLOSED' | `SELECT COUNT(*) FROM complaints WHERE status != 'CLOSED'` |
| `assignedComplaints` | assigned_to IS NOT NULL | `SELECT COUNT(*) FROM complaints WHERE assigned_to IS NOT NULL` |
| `inProgressComplaints` | status = 'IN_PROGRESS' | `SELECT COUNT(*) FROM complaints WHERE status = 'IN_PROGRESS'` |
| `resolvedComplaints` | status = 'RESOLVED' | `SELECT COUNT(*) FROM complaints WHERE status = 'RESOLVED'` |
| `highPriorityComplaints` | priority IN ('HIGH', 'EMERGENCY') | `SELECT COUNT(*) FROM complaints WHERE priority IN ('HIGH','EMERGENCY')` |
| `recentComplaints` | last 5 by reportedAt | `SELECT * FROM complaints ORDER BY created_at DESC LIMIT 5` |
| `resolutionRate` | RESOLVED / total * 100 | `SELECT COUNT(CASE WHEN status='RESOLVED' THEN 1 END)*100.0/COUNT(*) FROM complaints` |

### 3.5 Actuator Endpoints

| Method | Endpoint | Description | Auth |
|--------|----------|-------------|------|
| GET | `/actuator/health` | Spring Boot health check | No |
| GET | `/actuator/info` | Application build info | No |
| GET | `/actuator/logs` | Application logs (recent) | No (or restricted) |

**Health Check Response**:
```json
{
  "status": "UP",
  "components": {
    "database": {
      "status": "UP",
      "details": {
        "message": "Connection to localhost:3306 established successfully"
      }
    },
    "application": {
      "status": "UP"
    }
  }
}
```

## 4. Error Response Format

All error responses follow a consistent format:

```json
{
  "status": 400,
  "error": "Bad Request",
  "message": "Specific error message describing what went wrong",
  "timestamp": "2026-01-15T10:30:00Z",
  "path": "/complaints"
}
```

### Common Error Codes

| Code | Meaning | Example |
|------|---------|---------|
| 400 | Bad Request | Validation failed, invalid input |
| 401 | Unauthorized | Missing or invalid login |
| 403 | Forbidden | Insufficient permissions for role |
| 404 | Not Found | Requested resource doesn't exist |
| 409 | Conflict | Invalid status transition |
| 500 | Internal Server Error | Unexpected exception |

### Example Error Responses

**Validation Error**:
```json
{
  "status": 400,
  "error": "Validation Failed",
  "messages": [
    {"field": "streetName", "error": "Street name is required"},
    {"field": "ward", "error": "Ward must not be empty"}
  ],
  "timestamp": "2026-01-15T10:30:00Z"
}
```

**Unauthorized Access**:
```json
{
  "status": 401,
  "error": "Unauthorized",
  "message": "Authentication required",
  "timestamp": "2026-01-15T10:30:00Z"
}
```

**Forbidden (Wrong Role)**:
```json
{
  "status": 403,
  "error": "Forbidden",
  "message": "Insufficient permissions: ADMIN role required",
  "timestamp": "2026-01-15T10:30:00Z"
}
```

**Not Found**:
```json
{
  "status": 404,
  "error": "Not Found",
  "message": "Complaint with id=999 not found",
  "timestamp": "2026-01-15T10:30:00Z"
}
```

## 5. HTTP Status Code Summary

| Status Code | When Returned |
|-------------|---------------|
| 200 | Successful GET request; form display |
| 201 | Successful POST creation (new complaint) |
| 202 | Request accepted (async operation) |
| 204 | Successful DELETE or update without content |
| 302 | Redirect after form submit, status change |
| 400 | Validation error, invalid data, bad transition |
| 401 | Missing/Invalid authentication |
| 403 | Authorization failed (wrong role) |
| 404 | Resource not found (invalid ID) |
| 409 | Conflict (invalid status transition) |
| 500 | Unexpected server error |

## 6. Thymeleaf Template Integration

The application primarily uses Thymeleaf for server-side rendering. Key integration points:

### 6.1 Form Submission

```html
<!-- Example complaint form -->
<form th:action="@{/complaints}" th:method="POST" class="needs-validation" novalidate>
    <input type="text" th:field="*{streetName}" required />
    <input type="text" th:field="*{ward}" required />
    <select th:field="*{faultCategory}" required>
        <option value="">Select category</option>
        <option value="NOT_WORKING">Not Working</option>
        <option value="FLICKERING">Flickering</option>
        <option value="DAMAGED_POLE">Damaged Pole</option>
        <option value="EXPOSED_WIRING">Exposed Wiring</option>
        <option value="OTHER">Other</option>
    </select>
    <input type="text" th:field="*{description}" required />
    <select th:field="*{priority}" required>
        <option value="">Select priority</option>
        <option value="LOW">Low</option>
        <option value="MEDIUM">Medium</option>
        <option value="HIGH">High</option>
        <option value="EMERGENCY">Emergency</option>
    </select>
    <button type="submit" class="btn btn-primary">Submit</button>
</form>
```

### 6.2 Role-Based Access

```html
<!-- Show admin-only button -->
<th:block th:secures="hasRole('ADMIN')">
    <a th:href="@{/complaints/{id}/assign(technicianId=${technician.id})}" class="btn btn-warning">Assign</a>
</th:block>

<!-- Show technician-only button -->
<th:block th:secures="hasRole('TECHNICIAN')">
    <button th:action="@{/complaints/{id}/status(newStatus=${newStatus})}" class="btn btn-primary">Update Status</button>
</th:block>
```

### 6.3 Displaying Validation Errors

```html
<div class="alert alert-danger" role="alert">
    <strong>Please correct the errors below:</strong>
    <div th:errors="*{streetName}" th:if="*{hasErrors()}">Street name error</div>
    <div th:errors="*{ward}" th:if="*{hasErrors()}">Ward error</div>
    <div th:errors="*{priority}" th:if="*{hasErrors()}">Priority error</div>
</div>
```

### 6.4 Displaying Messages

```html
<!-- Success message after redirect -->
<div th:if="${message}" class="alert alert-success" role="alert">
    <span th:text="${message}">Success message</span>
</div>

<!-- Error message after failed operation -->
<div th:if="${error}" class="alert alert-danger" role="alert">
    <span th:text="${error}">Error message</span>
</div>
```

## 7. API Versioning (Planned)

The API may be versioned in the future, but for MVP:

- **No version prefix** on endpoints (all under `/complaints`, `/dashboard`, etc.)
- **Semantic versioning** applied at application level if API evolves
- **Deprecation strategy**: new endpoints added alongside existing; old ones marked deprecated after 3 months

---