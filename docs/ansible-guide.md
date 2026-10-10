# Ansible Configuration Management Guide

## 1. Overview

Ansible is used for configuration management and deployment automation of the Automated Streetlight Fault Reporting System. The Ansible playbook configures a Linux target with required packages, deployment directories, application configuration, Docker prerequisites, service availability, and deployment health checks.

**Target**: Linux server (Ubuntu 22.04 or CentOS 9 recommended)  
**Ansible Version**: 9.x+  
**Idempotency**: Playbook runs successfully twice with no unnecessary changes (`changed=0` on second run)

## 2. Directory Structure

```
ansible/
├── inventory.yml          ← Target host(s) and connection configuration
├── vars.yml               ← Variables (placeholder values, not secrets)
├── site.yml               ← Main playbook (orchestration)
├── roles/
│   ├── system             ← Package installation, users, directories
│   ├── docker             ← Docker installation, service management
│   ├── application        ← Copy configs, deploy app, restart services
│   └── health             ← Health checks, status verification
├── templates/             ← Jinja2 templates for config files
└── files/                 ← Static files to copy (scripts, etc.)
```

## 3. Inventory File (`inventory.yml`)

### 3.1 Inventory Configuration

```yaml
# inventory.yml - Target host configuration

# =============================================================
# SINGLE TARGET (for college project demonstration)
# =============================================================
[streetlight-app]
target-host ansible_host=<TARGET_IP> ansible_user=<SSH_USER> ansible_ssh_pass=<SSH_PASSWORD_OR_KEY>

# =============================================================
# OR MULTIPLE TARGETS (uncomment as needed)
# =============================================================
# [streetlight-app]
# target-host1 ansible_host=192.168.1.10 ansible_user=deploy_user
# target-host2 ansible_host=192.168.1.11 ansible_user=deploy_user

[streetlight-app:vars]
# Connection settings
ansible_python_interpreter=/usr/bin/python3

# Deployment settings (can be overridden in vars.yml)
deploy_dir=/opt/streetlight
docker_image=streetlight/fault-reporting
docker_tag=dev
```

**Important**: 
- `<TARGET_IP>` = IP address of the Linux target server
- `<SSH_USER>` = SSH username (e.g., `ubuntu`, `root`, `deploy`)
- `<SSH_PASSWORD_OR_KEY>` = SSH password OR path to private key

**For without SSH access**: Document the required permissions and provide setup instructions for clean target provisioning.

### 3.2 Connection Configuration

| Parameter | Description | Example |
|-----------|-------------|---------|
| `ansible_host` | Target server IP address | `192.168.1.50` |
| `ansible_user` | SSH username | `deploy` |
| `ansible_ssh_pass` | SSH password (for password auth) | `your_password` |
| `ansible_ssh_private_key_file` | Path to private key (for key auth) | `~/.ssh/id_rsa` |
| `ansible_python_interpreter` | Python path for Ansible | `/usr/bin/python3` |

**To test connection**:
```bash
ansible -i inventory.yml all -m ping
# Should output: target-host | SUCCESS => {"changed": false, "ansible_facts": ..., "ping": "pong"}
```

## 4. Variables File (`vars.yml`)

### 4.1 Variables Configuration

```yaml
# vars.yml - Variable values (placeholder values for demo)

# =============================================================
# DATABASE CONFIGURATION
# =============================================================
db_host: localhost
db_port: "3306"
db_name: streetlight
db_user: streetlight
db_password: CHANGE_ME_PRODUCTION  # <-- placeholder, not real password

# =============================================================
# APPLICATION CONFIGURATION
# =============================================================
app_name: streetlight-fault-reporting
app_url: http://localhost:8080
server_port: 8080

# =============================================================
# DOCKER CONFIGURATION
# =============================================================
docker_image: streetlight/fault-reporting
docker_tag: dev  # will be overridden by Jenkins pipeline tag
docker_compose_file: docker-compose.yml

# =============================================================
# NGINX CONFIGURATION
# =============================================================
nginx_port: 80
nginx_https_port: 443
nginx_domain: localhost

# =============================================================
# SECURITY - PLACEHOLDERS ONLY
# =============================================================
# Never commit real passwords or keys. These are examples only.
# Replace with actual values via --extra-vars or environment variables.
bcrypt_strength: 10

# =============================================================
# ANALYTICS / METRICS
# =============================================================
resolution_rate_metric: false  # set true after initial deployment
```

**Key Points**:
- All password/credential values are **placeholders** (`CHANGE_ME_PRODUCTION`)
- Real values should be provided at runtime via `--extra-vars` or environment variables
- `db_password` must NOT be a real password in this file
- Variables can be overridden per execution

### 4.2 Variable Precedence (Ansible Order)

Lower precedence → Higher precedence (later wins):

1. Default values in `defaults/` (role defaults)
2. `vars/` in playbook
3. `vars/` in inventory
4. `extra-vars` (command-line `--extra-vars` or environment variables)
5. Environment variables

**Example overriding at runtime**:
```bash
ansible-playbook -i inventory.yml site.yml --extra-vars "db_password=my_real_password"
```

## 5. Main Playbook (`site.yml`)

### 5.1 Playbook Structure

```yaml
# site.yml - Main orchestration playbook

- name: Configure System Packages and Directories
  hosts: streetlight-app
  become: yes  # runs as root (or with sudo)
  tags: [system, packages]

  roles:
    - system

- name: Install and Configure Docker
  hosts: streetlight-app
  become: yes
  tags: [docker, docker-install]

  roles:
    - docker

- name: Deploy Application
  hosts: streetlight-app
  become: yes
  tags: [application, deploy]

  roles:
    - application

- name: Verify Health and Idempotency
  hosts: streetlight-app
  become: yes
  tags: [health, verify]

  tasks:
    - name: Run health check endpoint
      ansible.builtin.command: curl -f http://localhost:8080/actuator/health
      register: health_check
      failed_when: health_check.rc != 0
      changed_when: false

    - name: Display health check result
      ansible.builtin.debug:
        msg: "Application health: {{ 'UP' if health_check is success else 'DOWN' }}"

    - name: Verify idempotency (second run note)
      ansible.builtin.debug:
        msg: "Idempotency: Second run should produce no unnecessary changes (changed=0)"
```

### 5.2 Roles Structure

#### 5.2.1 `roles/system/` - System Configuration

```yaml
# roles/system/tasks/main.yml

- name: Update package cache
  ansible.builtin.apt:
    update_cache: yes
    cache_valid_time: 3600
  when: ansible_os_family == "Debian"

- name: Install required packages
  ansible.builtin.package:
    name:
      - curl
      - wget
      - apt-transport-https
      - gnupg
      - lsb-release
    state: present

- name: Create deployment directory
  ansible.builtin.file:
    path: "{{ deploy_dir }}"
    state: directory
    owner: "{{ ansible_user }}"
    group: "{{ ansible_user }}"
    mode: '0755'

- name: Create log directory
  ansible.builtin.file:
    path: "{{ deploy_dir }}/logs"
    state: directory
    owner: "{{ ansible_user }}"
    group: "{{ ansible_user }}"
    mode: '0755'

- name: Create data directory
  ansible.builtin.file:
    path: "{{ deploy_dir }}/data"
    state: directory
    owner: "{{ ansible_user }}"
    group: "{{ ansible_user }}"
    mode: '0755'

- name: Create config directory
  ansible.builtin.file:
    path: "{{ deploy_dir }}/config"
    state: directory
    owner: "{{ ansible_user }}"
    group: "{{ ansible_user }}"
    mode: '0755'

- name: Ensure swap is disabled (required for Docker)
  ansible.builtin.command: "swapoff -a"
  changed_when: false
  when: ansible_swaptotal_mb != null

- name: Disable swap permanently (Ubuntu)
  ansible..lineinfile:
    path: /etc/fstab
    regexp: "^\s* swap "
    state: absent
  when: ansible_os_family == "Debian"
```

#### 5.2.2 `roles/docker/` - Docker Installation

```yaml
# roles/docker/tasks/main.yml

- name: Add Docker's official GPG key
  ansible.builtin.key:
    url: https://download.docker.com/linux/ubuntu/gpg
    state: present
  when: ansible_os_family == "Debian"

- name: Set up Docker stable repository
  ansible.builtin.apt_repository:
    repo: "deb [arch=$(dpkg --print-architecture)] https://download.docker.com/linux/ubuntu $(lsb_release -cs) stable"
    state: present
    update_cache: true
  when: ansible_os_family == "Debian"

- name: Install Docker Engine
  ansible.builtin.package:
    name:
      - docker-ce
      - docker-ce-cli
      containerd.io
    state: present
  when: ansible_os_family == "Debian"

- name: Install Docker Compose
  block:
    - name: Download Docker Compose binary
      ansible.builtin.get_url:
        url: https://github.com/docker/compose/releases/download/v2.40.2/docker-compose-$(uname -s)-$(uname -m)
        dest: /usr/local/bin/docker-compose
        mode: '0755'
    
    - name: Verify Docker Compose installation
      command: docker-compose version
      changed: false

- name: Start and enable Docker service
  ansible.builtin.service:
    name: docker
    state: started
    enabled: yes

- name: Add user to docker group (for docker without sudo)
  ansible.builtin.user:
    name: "{{ ansible_user }}"
    groups: docker
    append: yes
    state: present
  when: not ansible_check_mode
```

#### 5.2.3 `roles/application/` - Application Deployment

```yaml
# roles/application/tasks/main.yml

- name: Copy Docker Compose file
  ansible.builtin.copy:
    src: "{{ docker_compose_file }}"
    dest: "{{ deploy_dir }}/docker-compose.yml"
    owner: "{{ ansible_user }}"
    group: "{{ ansible_user }}"
    mode: '0644'

- name: Copy .env file (from host environment)
  ansible.builtin.copy:
    src: .env
    dest: "{{ deploy_dir }}/config/.env"
    owner: "{{ ansible_user }}"
    group: "{{ ansible_user }}"
    mode: '0600'
  when: file_exists(".env")

- name: Copy application configuration
  ansible.builtin.copy:
    src: application.yml
    dest: "{{ deploy_dir }}/config/application.yml"
    owner: "{{ ansible_user }}"
    group: "{{ ansible_user }}"
    mode: '0644'

- name: Pull Docker image from registry
  community.docker.docker_image:
    name: "{{ docker_image }}"
    tag: "{{ docker_tag }}"
    source: pull
  register: pull_result

- name: Start application with Docker Compose
  community.docker.docker_compose:
    project_name: streetlight
    - state: started
    - pull: always
    # Note: depends on mysql service being configured separately
    # or use: file: "{{ deploy_dir }}/docker-compose.yml"

- name: Wait for application to be healthy
  ansible.builtin.wait_for:
    host: localhost
    port: 8080
    delay: 5
    timeout: 60
    state: started

- name: Display deployment status
  ansible.builtin.debug:
    msg: "Application deployed: {{ docker_image }}:{{ docker_tag }}"
```

#### 5.2.4 `roles/health/` - Health Checks

```yaml
# roles/health/tasks/main.yml

- name: Check application health endpoint
  ansible.builtin.uri:
    url: http://localhost:8080/actuator/health
    method: GET
    status_code: 200
    timeout: 10
    return_content: yes
  register: health_response

- name: Display health status
  ansible.builtin.debug:
    msg: "Health check result: {{ health_response.json.status }} (status: {{ health_response.status }})"

- name: Verify database connectivity (indirect health check)
  ansible.builtin.uri:
    url: http://localhost:8080/actuator/info
    method: GET
    timeout: 10
    return_content: yes
  register: info_response

- name: Display application info
  ansible.builtin.debug:
    msg: "Application info: {{ info_response.json | ansible('json.query', 'build)')}}"

- name: Idempotency verification task
  ansible.builtin.assert:
    that:
      - health_response.json.status == "UP"
    fail_msg: "Application health check failed - deployment may be unhealthy"
    success_msg: "Application health check passed - system is healthy"

- name: Record deployment timestamp
  ansible.builtin.lineinfile:
    path: "{{ deploy_dir }}/logs/deployment.log"
    line: "Deployment: {{ ansible_date_time.iso8601_local }} | Image: {{ docker_image }}:{{ docker_tag }} | Host: {{ inventory_hostname }}"
    create: yes
    mode: '0644'
```

## 6. Templates (Jinja2)

### 6.1 Application Configuration Template

Create `roles/application/templates/application.yml.j2`:

```yaml
# application.yml.j2 - Template for application configuration

spring:
  application:
    name: {{ app_name }}
  datasource:
    url: jdbc:mysql://{{ db_host }}:{{ db_port }}/{{ db_name }}?useSSL=false&serverTimezone=UTC
    username: {{ db_user }}
    password: {{ db_password }}
    hikari:
      maximum-pool-size: 10
  jpa:
    hibernate:
      ddl-auto: none  # Flyway migrations
    show-sql: false
    properties:
      hibernate:
        dialect: org.hibernate.dialect.MySQL8Dialect
  flyway:
    enabled: true
    baseline-on-migrate: true
    locations: classpath:db/migration
    baseline-version: "0"
  profiles:
    active: {{ env.getOrDefault('SPRING_PROFILES_ACTIVE', 'dev') }}

server:
  port: {{ server_port }}

# Security
bcrypt:
  strength: {{ bcrypt_strength }}

# Application info
info:
  appUrl: {{ app_url }}
```

**Usage**: Ansible automatically processes Jinja2 templates (`.j2` files) and renders the variables.

### 6.2 Nginx Configuration Template (Optional)

Create `roles/system/templates/nginx.conf.j2` (if Nginx installed via Ansible):

```nginx
# nginx.conf.j2 - Reverse proxy configuration

upstream app_backend {
    server localhost:8080;
}

server {
    listen {{ nginx_port }};
    server_name {{ nginx_domain }};
    
    location / {
        proxy_pass http://app_backend;
        proxy_set_header Host $host;
        proxy_set_header X-Real-IP $remote_addr;
        proxy_set_header X-Forwarded-For $proxy_add_x_forwarded_for;
        proxy_set_header X-Forwarded-Proto $scheme;
    }
    
    location /actuator/ {
        proxy_pass http://app_backend/actuator/;
    }
    
    # Security headers
    add_header X-Content-Type-Options "nosniff" always;
    add_header X-Frame-Options "DENY" always;
}
```

## 7. Playbook Execution

### 7.1 First-Time Deployment

```bash
# 1. Navigate to Ansible directory
cd /path/to/ansible

# 2. Verify inventory can connect
ansible -i inventory.yml all -m ping

# 3. Run the playbook (first time - makes changes)
ansible-playbook -i inventory.yml site.yml \
  --ask-pass  # enter SSH password if prompted

# 4. Or with password file
# ansible-playbook -i inventory.yml site.yml -u deploy -k

# Expected output (first run - many changes):
# PLAY [streetlight-app] *****************************************************************************
# GATHER FACTS                                                                                      ok => [target-host]
# ...
# PLAY RECAP **********************************************************************************************
# target-host                    : ok=5   changed=12   unreachable=0   failed=0   skipped=0   rescued=0
```

### 7.2 Second Run (Idempotency Verification)

```bash
# Run the playbook again (should show minimal or no changes)
ansible-playbook -i inventory.yml site.yml \
  --ask-pass

# Expected output (second run - idempotent):
# PLAY [streetlight-app] *****************************************************************************
# GATHER FACTS                                                                                      ok => [target-host]
# ...
# PLAY RECAP **********************************************************************************************
# target-host                    : ok=5   changed=0   unreachable=0   failed=0   skipped=0   rescued=0
```

**Idempotency confirmed**: `changed=0` on second run means all tasks are idempotent (no unnecessary modifications).

### 7.3 Running with Specific Variables

```bash
# Override variables at runtime
ansible-playbook -i inventory.yml site.yml \
  --extra-vars "db_password=my_real_password docker_tag=v1.0.0"

# Or using a variables file
ansible-playbook -i inventory.yml site.yml \
  -vars-file my-custom-vars.yml
```

### 7.4 Playbook with Tags (Selective Execution)

```bash
# Run only system/package tasks
ansible-playbook -i inventory.yml site.yml --tags "system,packages"

# Run only Docker installation
ansible-playbook -i inventory.yml site.yml --tags "docker,docker-install"

# Run only application deployment
ansible-playbook -i inventory.yml site.yml --tags "application,deploy"

# Run only health checks
ansible-playbook -i inventory.yml site.yml --tags "health,verify"
```

## 8. Recovery and Rollback

### 8.1 Recovery from Failed Deployment

```bash
# If playbook fails partway through:

# Option 1: Re-run the playbook (Ansible resumes from where it failed)
ansible-playbook -i inventory.yml site.yml --ask-pass

# Option 2: Run specific roles to fix
ansible-playbook -i inventory.yml site.yml --roles-path roles \
  --tags "docker"  # re-run Docker role

# Option 3: Manual recovery steps
# 1. Stop Docker containers
docker compose -f /opt/streetlight/docker-compose.yml down

# 2. Pull and restart with correct image
docker pull streetlight/fault-reporting:v1.0.0
docker compose -f /opt/streetlight/docker-compose.yml up -d

# 3. Re-run Ansible playbook for idempotent recovery
ansible-playbook -i inventory.yml site.yml --ask-pass
```

### 8.2 Rollback Procedure

```bash
# If deployment causes application failure:

# 1. Identify previous known-good deployment
#    - From Docker image history: docker images
#    - Or from Ansible deployment log: cat /opt/streetlight/logs/deployment.log

# 2. Rollback to previous image
docker pull streetlight/fault-reporting:v1.0.0  # previous tag
docker tag streetlight/fault-reporting:v1.0.0 streetlight/fault-reporting:latest

# 3. Restart application
docker compose -f /opt/streetlight/docker-compose.yml up -d

# 4. Verify health check
curl http://localhost:8080/actuator/health
# Should return: {"status":"UP",...}

# 5. Re-run Ansible for idempotent state
ansible-playbook -i inventory.yml site.yml --ask-pass

# 6. Verify: changed=0 (idempotent), health check passes
```

### 8.3 Ansible Idempotency Best Practices

| Practice | Description |
|----------|-------------|
| **Use idempotent modules** | `apt`, `file`, `service`, `docker_image`, `docker_compose` are inherently idempotent |
| **Avoid `shell`/`command` when possible** | Use Ansible modules instead; they have built-in idempotency |
| **Use `changed_when: false`** | For commands that don't produce meaningful `changed` output |
| **Use `creates`/`removes` parameters** | For `command`/`shell` when absolute required |
| **Test idempotency** | Run playbook twice; verify `changed=0` on second run |
| **Document non-idempotent tasks** | If a task cannot be made idempotent, document why |

**Idempotency verification command**:
```bash
# First run (makes changes)
ansible-playbook -i inventory.yml site.yml

# Second run (should show changed=0)
ansible-playbook -i inventory.yml site.yml

# Compare: if second run has changed=0, idempotency confirmed
```

## 9. Target Provisioning (Clean Target Setup)

### 9.1 Required Permissions

| Action | Required Permission | Notes |
|--------|--------------------|-------|
| SSH access | To target server | `ssh user@target-ip` |
| Package installation | Root/sudo | `apt install` or `yum install` |
| Docker installation | Root/sudo | May require kernel modules |
| Docker daemon access | Docker group | `usermod -aG docker $USER` |
| Port 80/443 binding | Root or specific capabilities | Nginx binding |
| MySQL data directory | Write access | `/var/lib/mysql` |
| `/opt/` directory | Create and write | Deployment directory |
| `/etc/nginx/` | Read/write (if Nginx) | Reverse proxy config |
| `/etc/fstab` | Modify (disable swap) | Required for Docker |
| Log directory | Create and write | `/opt/streetlight/logs` |

### 9.2 Clean Target Setup Checklist

```bash
# 1. Provision new Linux server (Ubuntu 22.04 recommended)
#    - Minimum: 2 CPU cores, 4 GB RAM, 2 GB disk space
#    - OS: Ubuntu 22.04 LTS or CentOS 9

# 2. Create SSH user with sudo access
useradd -m -s /bin/bash deploy
usermod -aG sudo deploy
# Set password: passwd deploy

# 3. SSH into server and authenticate
ssh deploy@<target-ip>

# 4. Disable swap (required for Docker)
sudo swapoff -a
# Permanently: edit /etc/fstab and remove swap line

# 5. Install prerequisites
sudo apt update
sudo apt install -y curl wget gnupg lsb-release

# 6. Install Docker (via Ansible playbook or manually)
#   - Or: curl -fsSL https://get.docker.com -o get-docker.sh
#   - Or: let Ansible handle it (recommended)

# 7. (Optional) Install Nginx
sudo apt install -y nginx

# 8. Test SSH from Ansible control machine
ansible -i inventory.yml all -m ping
# Should: target-host | SUCCESS => {"ping": "pong"}

# 9. Run Ansible playbook for first deployment
ansible-playbook -i inventory.yml site.yml --ask-pass

# 10. Verify: application accessible, health check passes
#    - Open browser: http://<target-ip>:8080
#    - Health check: curl http://<target-ip>:8080/actuator/health
```

### 9.3 Post-Deployment Verification

```bash
# After Ansible playbook completes:

# 1. Check running containers
docker ps
# Should show: streetlight-app, streetlight-mysql (if in compose)

# 2. Verify application access
curl -s http://localhost:8080/actuator/health
# Expected: {"status":"UP","components":{...}}

# 3. Access web interface
# Open browser to: http://localhost:8080
# Or: http://<target-ip>:8080

# 4. Test login with default credentials
# Username: admin, Password: admin123 (change immediately)

# 4. Check deployment log
cat /opt/streetlight/logs/deployment.log
# Should show: deployment timestamp, image tag, host name

# 5. Verify idempotency (run playbook again)
ansible-playbook -i inventory.yml site.yml
# Expected: changed=0 on second run

# 6. Check Ansible output for: "ok=5 changed=0 unreachable=0 failed=0"
```

## 10. Known Limitations

| Limitation | Description | Workaround |
|------------|-------------|------------|
| **SSH access required** | Ansible assumes SSH access to target | Document manual setup steps (see section 9) |
| **Passwordless SSH not configured** | May need `ansible_ssh_pass` each run | Use SSH keys: `ssh-keygen` + `ansible-connection: local` or push key to target |
| **Docker not pre-installed** | Playbook installs Docker, but takes time | Be patient during first run; subsequent runs faster |
| **Swap must be disabled** | Docker requires swap disabled | Playbook includes task to disable; manual servers need manual disable |
| **MySQL 8 specific** | Entities configured for MySQL 8 dialect | If using different MySQL version, adjust dialect |
| **Nginx optional** | Reverse proxy not required for local dev | Playbook can skip Nginx role if not needed |
| **No Kubernetes** | This playbook is for Docker Compose only | Kubernetes deployment would require separate playbooks |
| **Single target only** | Designed for one server deployment | Multi-server requires inventory changes and role adjustments |
| **No cert-manager** | SSL certificate automation not included | Manual SSL cert placement, or use self-signed for testing |
| **Limited user management** | Only creates deployment directory user | Full user management would require additional IAM setup |

## 11. troubleshooting

### 11.1 Common Issues and Solutions

| Problem | Likely Cause | Solution |
|---------|--------------|----------|
| **Playbook fails with "Connection refused"** | Docker not running; wrong host/port | `docker start`; check `inventory.yml` host/port |
| **Health check fails (525 or 523)** | Application not started; MySQL not ready | `docker compose up -d`; wait for MySQL; check `depends_on` |
| **Connection timeout to actuator** | Firewall blocking 8080; app not listening | Check `docker compose ps`; ensure port mapping `8080:8080` |
| **MySQL authentication error** | Wrong `db_password` in .env or vars.yml | Verify `.env` file; use correct BCrypted password |
| **Flyway migration error** | Schema already exists; baseline not set | Check `flyway.conf`; ensure `baseline-on-migrate: true` |
| **403 Forbidden on app pages** | Spring Security configuration issue | Check `SecurityConfig.java`; ensure `.antMatchers` for static resources |
| **Selenium tests fail (browser not found)** | ChromeDriver version mismatch; no display in CI | Use headless mode: `--headless --no-sandbox --disable-dev-shm-usage` |
| **Docker compose up fails** | Port already in use; compose file errors | `docker compose down`; fix port mappings; validate YML syntax |
| **Ansible "unreachable" error** | SSH access denied; wrong credentials | Verify SSH credentials; test with `ansible -i inventory.yml all -m ping` |
| **Variable substitution failed** | Jinja2 template syntax error | Check `.j2` template files; ensure `{{ variable }}` syntax correct |
| **Playbook stops mid-way** | Error in earlier task; `ignore_errors: false` (default) | Fix the first error; re-run playbook (Ansible resumes) |
| **Idempotency not working** | Task modifies state each run | Review task: use idempotent modules; add `changed_when: false` if needed |
| **Docker image pull fails** | Wrong image name/tag; credentials issue | Verify `docker_image` and `docker_tag`; check Docker Hub access |
| **Nginx won't start** | Port 80/443 already in use; config error | `lsof -i :80`; check nginx config syntax `nginx -t`; fix conflicts |

### 11.2 Debugging Commands

```bash
# 1. Test Ansible connection
ansible -i inventory.yml all -m ping

# 2. Check Ansible facts (debug what's available)
ansible -i inventory.yml all -a "setup" 2>&1 | head -30

# 3. Run playbook with more verbosity
ansible-playbook -i inventory.yml site.yml -v

# 4. Run playbook with --check (dry run)
ansible-playbook -i inventory.yml site.yml --check

# 5. Docker troubleshooting
docker ps -a                         # show all containers (including stopped)
docker images                        # show all images
docker logs <container_name>         # show container logs
docker system df                     # show disk usage

# 6. Application health checks
curl -s http://localhost:8080/actuator/health
curl -s http://localhost:8080/actuator/info

# 7. MySQL client connection
mysql -u streetlight -p streetlight  # connect to database
show databases;
use streetlight;
show tables;

# 8. Ansible-lint (if installed)
ansible-lint site.yml                # check for best practices

# 9. Review playbook output for task details
#    Re-run with -t (tag) to isolate problematic task
ansible-playbook -i inventory.yml site.yml --tags "application" -v
```

### 11.3 Getting Help

- **Ansible documentation**: https://docs.ansible.com/
- **Docker documentation**: https://docs.docker.com/
- **Spring Boot Actuator**: https://spring.io/guides/gs/actuator-service/
- **Project issues**: GitHub Issues in the repository
- **Community**: Ansible Community Forum, Docker Community Forums

---