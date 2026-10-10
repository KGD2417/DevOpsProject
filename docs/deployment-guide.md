# Deployment Guide

## 1. Prerequisites

Before deploying the Automated Streetlight Fault Reporting System, ensure the following are available:

### 1.1 Software Requirements

| Software | Version | Notes |
|----------|---------|-------|
| **Java 21 JDK** | OpenJDK 21 | Download from Oracle or AdoptOpenJDK |
| **Maven 3.9.x** | 3.9.6+ | Use `./mvnw` wrapper (included) or install globally |
| **Docker Desktop** | 4.+ (4.49.0 confirmed working) | Includes Docker Engine + Compose |
| **Docker Compose** | v2.40.2+ | Included with Docker Desktop |
| **MySQL 8.0** | 8.0+ | Can run via Docker Compose or standalone |
| **Git** | 2.+ | For cloning and version control |
| **Chrome Browser** | Latest | For Selenium tests (headless in CI) |
| **Text Editor / IDE** | Any | IntelliJ IDEA, Eclipse, VS Code, or notepad |

### 1.2 Hardware Requirements

| Resource | Minimum | Recommended |
|----------|---------|-------------|
| **CPU** | 2 cores | 4+ cores |
| **RAM** | 4 GB | 8+ GB |
| **Disk Space** | 2 GB free | 5+ GB (for Docker images, logs) |
| **Network** | Ethernet or WiFi | Stable local network |

### 1.3 Repository Setup

```bash
# 1. Clone the repository
git clone https://github.com/your-username/streetlight-fault-reporting.git
cd streetlight-fault-reporting

# 2. Check out the develop branch (or main if early)
git checkout develop

# 3. Review environment setup
copy .env.example .env
# Edit .env with your values (NOT committed)

# 4. Verify Maven Wrapper is executable
chmod +x mvnw  # Linux/macOS
# Windows: right-click mvnw.cmd → Properties → Unblock

# 5. Verify Docker is running
docker --version
docker compose version
```

## 2. Local Development Deployment

### 2.1 Start All Services via Docker Compose

```bash
# From the project root directory

# 1. Start MySQL + application (with Nginx reverse proxy)
docker compose up -d

# 2. Check running containers
docker compose ps

# Expected output:
# NAME               COMMAND               SERVICE           STATUS
# streetlight-mysql  "docker-entrypoint.sh"   mysql       running
# streetlight-app    "startup.sh"            app           running
# streetlight-nginx  "nginx -g 'daemon off;'"  nginx       running

# 3. View logs (if needed)
docker compose logs -f app

# 4. Access the application
# - Direct: http://localhost:8080
# - Via Nginx: http://localhost (if Nginx configured)
```

### 2.2 Application Access URLs

| Access Method | URL | Description |
|---------------|-----|-------------|
| **Direct Application** | `http://localhost:8080` | Spring Boot embedded Tomcat |
| **Nginx Reverse Proxy** | `http://localhost` | If Nginx configured (port 80) |
| **Nginx with SSL** | `https://localhost` | If HTTPS configured (port 443) |
| **MySQL Direct** | `jdbc:mysql://localhost:3306/streetlight` | For database client (Workbench, DBeaver) |
| **Actuator Health** | `http://localhost:8080/actuator/health` | Spring Boot health check |

### 2.3 Default Login Credentials (Development Only)

| Role | Username | Password | Notes |
|------|----------|----------|-------|
| **Admin** | `admin` | `admin123` | **Change password on first login** |
| **Technician** | `technician1` | `tech123` | Temporary; change preferred |
| **Citizen** | `citizen1` | `citizen123` | Temporary; change preferred |

**Important**: These default credentials are for development only. They are **not** committed to source code (listed in `.gitignore`). Always change passwords after first login.

### 2.4 Stopping and Restarting

```bash
# Stop all services (containers)
docker compose down

# Restart services (same as up -d)
docker compose up -d

# Restart only the application (restart container)
docker restart streetlight-app

# Restart only MySQL
docker restart streetlight-mysql
```

### 2.5 Application Logs

```bash
# View all service logs
docker compose logs

# View specific service logs
docker compose logs -f app    # application logs
docker compose logs -f mysql  # database logs
docker compose logs -f nginx  # reverse proxy logs

# Follow live output (Ctrl+C to exit)
docker compose logs -f
```

### 2.6 Manual Application Restart (without Docker Compose)

If running the application directly with Maven (not recommended for production):

```bash
# Stop any running java process
pkill -f "streetlight"  # Linux/macOS
# Windows: taskkill /IM java.exe /F

# Run with Maven
./mvnw spring-boot:run

# Or build and run the JAR
./mvnw clean package
java -jar target/streetlight-fault-reporting-1.0.0.jar
```

## 3. Production Deployment with Docker Compose

### 3.1 Production docker-compose.yml Adjustments

The `docker-compose.yml` included is suitable for both development and production with some adjustments:

**For production, ensure these settings:**

```yaml
# Key production adjustments:

app:
  # Use specific version tag instead of latest
  image: ${DOCKER_IMAGE}:${DOCKER_TAG}  # e.g., v1.0.0
  
  # Restart policy
  restart: unless-stopped
  
  # Resource limits (prevent resource exhaustion)
  deploy:
    resources:
      limits:
        memory: 512M
        cpus: '0.5'
  
  # Environment variables from .env file (production values)
  env_file:
    - .env.production

# MySQL production settings
mysql:
  # Persistent volume for data persistence
  volumes:
    - mysql_data:/var/lib/mysql
    - ./mysql-backup:/backup  # optional backup directory
  
  # MySQL root password from secure source
  environment:
    - MYSQL_ROOT_PASSWORD_FILE=/run/secrets/mysql_root_password
  
  # Health check
  healthcheck:
    test: ["CMD", "mysqladmin", "ping", "-u", "root"]
    interval: 10s
    timeout: 5s
    retries: 5

# Nginx production
nginx:
  # Port mappings (80/443 for production)
  ports:
    - "80:80"
    - "443:443"
  
  # SSL configuration (if needed)
  volumes:
    - ./nginx/ssl:/etc/nginx/ssl:ro
    - ./nginx/certs:/etc/nginx/certs:ro
```

### 3.2 Environment Variable Configuration

#### 3.2.1 `.env` File (Development - committed placeholder values allowed)

```env
# .env (development - can have real values for local dev)
DB_HOST=localhost
DB_PORT=3306
DB_NAME=streetlight
DB_USER=streetlight
DB_PASSWORD=streetlight123  # OK for dev, not committed to Git

APP_NAME=streetlight-fault-reporting
APP_URL=http://localhost:8080
SERVER_PORT=8080

BCRYPT_STRENGTH=10

DOCKER_IMAGE=streetlight/fault-reporting
DOCKER_TAG=dev

# Nginx
NGINX_PORT=80
```

#### 3.2.2 `.env.example` (Committed to Repo - placeholder values only)

```env
# .env.example (IN committed to Git - never real values)
DB_HOST=localhost
DB_PORT=3306
DB_NAME=streetlight
DB_USER=streetlight
DB_PASSWORD=CHANGE_ME_PRODUCTION  # <-- placeholder, not real password

APP_NAME=streetlight-fault-reporting
APP_URL=http://localhost:8080
SERVER_PORT=8080

BCRYPT_STRENGTH=10

DOCKER_IMAGE=streetlight/fault-reporting
DOCKER_TAG=dev
```

**Rule**: `.env.example` must contain only placeholder values like `CHANGE_ME_PRODUCTION`, never actual secrets. The `.env` file with real values is local-only and excluded by `.gitignore`.

#### 3.2.3 Jenkins Environment Variables

In Jenkins pipeline (Jenkinsfile), variables are sourced from credentials:

```groovy
environment {
    // From Docker Hub credentials (ID: docker-hub-credentials)
    REGISTRY = credentials('docker-hub-registry')
    IMAGE_NAME = 'streetlight/fault-reporting'
    IMAGE_TAG = "${env.BUILD_NUMBER}"
    DOCKER_CRED_ID = 'docker-hub-credentials'
    
    // From .env file or inline
    APP_NAME = 'streetlight-fault-reporting'
    
    // Quality gate settings
    DEPLOY_ENABLED = true
}
```

## 4. Nginx Reverse Proxy Configuration

### 4.1 Nginx Configuration

Create `nginx/nginx.conf` (or mount via volume):

```nginx
# nginx.conf - Reverse proxy for streetlight application

# HTTP server - redirect HTTP to HTTPS (if using SSL)
server {
    listen 80;
    server_name localhost;
    
    # Redirect all HTTP to HTTPS
    return 301 https://$host$request_uri;
}

# HTTPS server - secure proxy
server {
    listen 443 ssl;
    server_name localhost;
    
    # SSL certificates (if available)
    ssl_certificate /etc/nginx/certs/cert.crt;
    ssl_certificate_key /etc/nginx/certs/cert.key;
    ssl_protocols TLSv1.2 TLSv1.3;
    ssl_ciphers HIGH:!aNULL:!MD5;
    ssl_prefer_server_ciphers on;
    
    # Security headers
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "DENY" always;
    add_header X-XSS-Protection "1; mode=block" always;
    
    # Proxy to application backend
    location / {
        proxy_pass http://app:8080;
        proxy_http_version 1.1;
        proxy_set_header Upgrade $http_upgrade;
        proxy_set_header Connection "upgrade";
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
        
        # Timeout settings
        proxy_connect_timeout 60s;
        proxy_send_timeout 60s;
        proxy_read_timeout 60s;
    }
    
    # Serve static assets (if any) from separate location
    location /static/ {
        alias /var/www/static/;
    }
    
    # Health check endpoint (pass through to app)
    location /actuator/ {
        proxy_pass http://app:8080/actuator/;
    }
}
```

### 4.2 Nginx Docker Configuration

Update `docker-compose.yml` to include Nginx service:

```yaml
services:
  app:
    # ... (existing app config)
    ports:
      - "8080:8080"  # internal port 8080
    
  mysql:
    # ... (existing mysql config)
    
  nginx:
    image: nginx:alpine
    container_name: streetlight-nginx
    restart: unless-stopped
    ports:
      - "80:80"      # HTTP (redirect to HTTPS)
      - "443:443"    # HTTPS (secure proxy)
    volumes:
      - ./nginx:/etc/nginx/conf.d:ro  # mount nginx configs
      - ./certs:/etc/nginx/certs:ro   # SSL certificates (if used)
    depends_on:
      app:
        condition: service_healthy
      mysql:
        condition: service_healthy
```

### 4.3 Testing Nginx Configuration

```bash
# Start with Nginx
docker compose up -d

# Test HTTP redirect
curl -I http://localhost
# Should return: HTTP/1.1 301 Moved Permanently, location: https://localhost/

# Test HTTPS access
curl -I https://localhost
# Should return: 200 OK with application response

# Test application via Nginx
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP",...}

# Test direct application (bypass Nginx)
curl http://localhost:8080
# Should return: application HTML (login page or dashboard)
```

## 5. Rollback Procedure

### 5.1 Rollback Scenarios

| Scenario | Trigger | Rollback Method |
|----------|---------|-----------------|
| **Health check fails** (Stage 12) | After deployment, `/actuator/health` not UP | Restore previous Docker image |
| **Critical bug discovered** | Post-deployment bug report | Rollback to prior stable image |
| **Docker image corruption** | Image fails to start | Use known-good previous tag |
| **Database migration issue** | Flyway migration fails | Revert to prior schema version |

### 5.2 Rollback Steps (Step-by-Step)

```bash
# Step 1: Identify the previous (known-good) image tag
# Option A: From Docker history
docker images --sort=created streetlight/fault-reporting

# Option B: From Jenkins - previous build tag
# Jenkins stores: IMAGE_TAG from previous successful build
# e.g., v1.0.0 or v1.0.1 from before the failed deployment

# Step 2: Pull the previous image
docker pull registry.hub.docker.com/streetlight/fault-reporting:v1.0.0

# Step 3: Tag it as the current/latest (if needed)
docker tag registry.hub.docker.com/streetlight/fault-reporting:v1.0.0 streetlight/fault-reporting:latest

# Step 4: Restart with previous image using Docker Compose
docker compose down
docker compose up -d  # This will pull the tagged image (if tagged as latest)
# Or explicitly:
docker compose up -d streetlight-app  # restart app service only

# Step 5: Verify the rollback
docker compose ps
# Check: application container running with previous image

# Step 6: Run health check
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP",...}

# Step 7: Verify dashboard metrics are correct
# Access: http://localhost (or :8080)
# Confirm: all complaint data intact, no data loss

# Step 8: Document the rollback
# - Reason for rollback
# - Previous image tag used
# - Time of rollback
# - Verification results
```

### 5.3 Automated Rollback in Jenkins

```groovy
// In Jenkinsfile, Stage 12: Deployment Health Check
stage('12. Deployment Health Check') {
    steps {
        // Wait for app to be ready
        script {
            for (int i = 0; i < 30; i++) {
                def health = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health", returnStdout: true).trim()
                if (health == "200") {
                    echo "Application is healthy (HTTP 200)"
                    break
                }
                echo "Attempt ${i+1}: Application not ready, waiting 5s..."
                sleep(5)
            }
            
            // If we've exhausted retries without success
            if (currentBuild.result == 'FAILURE') {
                echo "Health check failed - initiating automated rollback"
                
                // Rollback to previous image tag stored in environment
                def previousTag = env.PREVIOUS_IMAGE_TAG ?: 'v1.0.0'
                sh "docker pull ${REGISTRY}/${IMAGE_NAME}:${previousTag}"
                sh "docker tag ${REGISTRY}/${IMAGE_NAME}:${previousTag} ${REGISTRY}/${IMAGE_NAME}:latest"
                sh "docker compose up -d"
                
                // Retry health check after rollback
                sleep(10)
                def newHealth = sh(script: "curl -s -o /dev/null -w '%{http_code}' http://localhost:8080/actuator/health", returnStdout: true).trim()
                
                if (newHealth != "200") {
                    error "Rollback failed - manual intervention required"
                } else {
                    echo "Rollback successful - application running on ${previousTag}"
                }
            }
        }
    }
}
```

### 5.4 Rollback Verification Checklist

- [ ] Application container running (docker ps)
- [ ] Health check endpoint returns UP (HTTP 200)
- [ ] Dashboard accessible and shows correct data
- [ ] All complaints intact (no data loss)
- [ ] User can login with existing credentials
- [ ] Status history preserved ( Flyway/DB not reverted in a way that loses data)
- [ ] Rollback documented in deployment log
- [ ] If rollback also fails: escalate to manual intervention, Docker Hub support

## 6. Recovery from Failed Deployment

### 6.1 Steps to Recovery

1. **Identify the failure point**:
   - Was it Docker build failure? (check build logs)
   - Was it Docker compose up failure? (check `docker compose logs`)
   - Was it health check failure? (check `/actuator/health`)
   - Was it Selenium test failure? (check Jenkins pipeline)

2. **Apply appropriate recovery**:

   **Failure: Docker build failed**
   ```bash
   # Check what went wrong
   docker build -t test-image .  # run manually to see errors
   
   # Fix the issue (Dockerfile, code, dependencies)
   # Rebuild and push
   docker build -t ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG} .
   docker push ${REGISTRY}/${IMAGE_NAME}:${IMAGE_TAG}
   ```

   **Failure: Docker compose up failed**
   ```bash
   # Check logs
   docker compose logs [service-name]
   
   # Common fixes:
   # - Ensure .env file has correct values
   # - Check MySQL is running: docker compose ps
   # - Verify ports not already in use
   # - Increase resource limits
   ```

   **Failure: Health check failed**
   ```bash
   # Check application status
   docker compose logs -f app
   curl http://localhost:8080/actuator/health
   
   # Common fixes:
   # - Ensure MySQL is running and accessible
   # - Check database migration (Flyway) ran: ./mvnw flyway:migrate
   # - Verify application.yml datasource config
   # - Check database connection pool (HikariCP)
   ```

   **Failure: Jenkins pipeline failed**
   ```bash
   # Review Jenkins build log
   # Fix the underlying issue (test failure, build error, etc.)
   # Push fix to GitHub; Jenkins triggers new pipeline run
   # Verify new pipeline completes all stages
   ```

3. **Re-run deployment** after root cause fixed:
   ```bash
   # Push the fix
   git add .
   git commit -m "fix: resolve deployment failure (specific issue)"
   git push origin develop  # or main
   
   # Jenkins webhook triggers new pipeline
   # Or: manually trigger Jenkins build
   ```

4. **Verify recovery**:
   - All pipelines pass
   - Application accessible
   - Health check passes
   - Dashboard metrics correct
   - Smoke test: create a test complaint, verify it appears

### 6.2 Post-Recovery Checklist

- [ ] Root cause documented
- [ ] Fix committed to Git (with conventional commit message)
- [ ] New Jenkins pipeline run completes successfully
- [ ] Application accessible at expected URL
- [ ] Health check endpoint returns UP
- [ ] Dashboard displays correct metrics from database
- [ ] No data loss verified (complaints, users, history intact)
- [ ] Screenshots taken of recovery verification
- [ ] Team notified of recovery and root cause
- [ ] If same failure recurs: consider additional safeguards

---