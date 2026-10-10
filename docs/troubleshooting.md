# Troubleshooting Guide

## 1. Overview

This guide provides solutions to common issues encountered during development, building, testing, deployment, and operation of the Automated Streetlight Fault Reporting System. Organized by category for quick reference.

## 2. Development Issues

### 2.1 Maven Build Failures

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `./mvnw clean compile` fails with `class not found` | Missing dependency; incomplete `mvnw` setup | Run `./mvnw wrapper:wrapper` to update wrapper; ensure `MAVEN_OPTS` not interfering |
| `Java version not supported` | Java version mismatch | Verify `java -version` outputs OpenJDK 21; update `pom.java.version` if needed |
| `Maven wrapper not executable` | `mvnw` permissions incorrect | `chmod +x mvnw` (Linux/macOS) or right-click `mvnw.cmd` → Properties → Unblock (Windows) |
| `Repository reconciliation failed` | Corrupted `.m2` cache | Delete `.m2` folder or run `./mvnw clean install -U` (update snapshots) |
| `Failed to resolve artifact org.springframework.boot:spring-boot-starter-parent` | Maven not downloading from Central | Check network connectivity; run with `-o` (offline) flag if needed; verify `settings.xml` |
| `BUILD FAILURE: Compilation error` | Code errors; missing imports | Fix compilation errors; ensure all `import` statements are correct; check for red squiggles in IDE |

### 2.2 Application Startup Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Address already in use: bind` | Port 8080 already occupied | `lsof -i :8080`; kill process on port; change `server.port` in `application.yml` |
| `Cannot determine embedded database driver` | Missing H2 or MySQL dependency | Ensure `spring-boot-starter-data-jpa` and database driver in `pom.xml` |
| `No qualifying bean of type` | Spring context fails to load | Check `@Component` annotations; ensure component scan base packages correct |
| `AccessDeniedException` (403) on login | Security config issue | Verify `WebSecurityConfigurerAdapter` (or SecurityFilterChain) configuration; check permitAll() settings |
| `BeanCreationException` | Circular dependency; wrong config | Review bean definitions; break circular dependencies; check `@Configuration` classes |
| `Application fails to start` on Docker | MySQL not ready when app starts | Ensure `depends_on` in `docker-compose.yml`; use health checks; add retry logic |
| `SQLSyntaxErrorException` | MySQL 8 dialect mismatch | Ensure `hibernate.dialect=org.hibernate.dialect.MySQL8Dialect` in `application.yml` |

### 2.3 Database Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Table not found: users` / `complaints` | Flyway migration not run; `ddl-auto=none` | Run `./mvnw flyway:migrate`; ensure `flyway.enabled=true` in `application.yml` |
| `Unknown column` error | Schema out of date; migration not applied | Check `flyway.info`; run `./mvnw flyway:rebase` if needed; verify migration files in `db/migration/` |
| `ConstraintViolationException` | Data violates NOT NULL or UNIQUE constraints | Validate entity annotations; ensure all NOT NULL fields have values; check unique constraints |
| `Connection refused` (MySQL) | MySQL not running; wrong port/host | `docker compose ps`; verify `DB_HOST`, `DB_PORT` in `.env`; `mysql -h localhost -P 3306` |
| `Authentication failed for user` | Wrong database username/password | Check `.env` DB_USER/DB_PASSWORD; verify MySQL user exists: `SELECT user, host FROM mysql.user;` |
| `H2 database different from MySQL` | Tests use H2, production uses MySQL | Ensure `spring.profiles.active=test` for tests; `prod` profile uses MySQL; entity annotations compatible |
| `Flyway baseline required` | First run on existing database | Run `./mvnw flyway:baseline`; set `baseline-on-migrate: true` in `application.yml` |

### 2.4 Java / JVM Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `UnsupportedClassVersionError` | Compiled with newer Java, older runtime | Use OpenJDK 21 for compilation; check `pom.source` and `pom.target` |
| `OutOfMemoryError: Java heap space` | Too little JVM memory | Increase `-Xmx` in `MAVEN_OPTS` or `spring.jvm.memory`; optimize data loading |
| `NoClassDefFoundError` | Classpath issue; missing dependency | Run `./mvnw dependency:tree`; ensure all dependencies resolved |
| `java: cannot find symbol` | Compilation error; wrong type | Check imports; ensure correct version of Java 21 features used |
| `SSLHandshakeException` | Certificate trust issue | Add self-signed cert to truststore; or disable SSL verification in test config |
| `Thread pool saturated` | Too many concurrent requests | Increase Spring thread pool size; optimize async processing |

## 3. Docker Issues

### 3.1 Docker Build Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Docker build fails` | Dockerfile errors; context issues | Check `Dockerfile` syntax; ensure `.dockerignore` excludes unnecessary files; build from project root |
| `layer cache not working` | File order in Dockerfile; `ADD`/`COPY` changes | Put changing files (JAR) later in Dockerfile; use `COPY target/...` last |
| `Too many levels of symbolic links` | Dockerfile context issues | Simplify directory structure; avoid symlinks in build context |
| `Java not found in PATH` | OpenJDK not installed in image | Use `openjdk:21-jdk` base image; or install JDK in Dockerfile via `apt-get install openjdk-21` |
| `Docker image too large` | Unnecessary files in image | Use `multistage build`; remove `apt-get install` remnants; use `RUNTIME` stage only |
| `build context too large` | Too many files sent to Docker daemon | Create `.dockerignore` file; exclude `target/`, `*.md`, `docs/`, `.git/` |

### 3.2 Docker Run Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Container exits immediately` | Application crash; entrypoint error; missing config | `docker compose logs app`; check `application.yml`; ensure `.env` file present |
| `Port 8080 already in use` | Another container or process uses 8080 | `docker compose down`; `lsof -i :8080`; change port mapping in `docker-compose.yml` |
| `MySQL connection refused` | MySQL service not running; wrong network | `docker compose ps`; ensure `mysql` service running; check `depends_on` |
| `Environment variables not visible` | `.env` file not loaded; wrong `env_file` config | Verify `env_file:` path in `docker-compose.yml`; use `export` vars explicitly |
| `Volume permission denied` | Docker volume ownership issues | `chown -R $(whoami):$(whoami) /opt/streetlight`; adjust ownership |
| `Out of memory: Killed` | Container OOM killed | Increase `mem_limit` in `docker-compose.yml`; optimize app memory usage |
| `Cannot connect to the Docker daemon` | Docker Desktop not running; socket issues | Start Docker Desktop; check `docker version`; restart Docker Desktop |
| `docker: 'compose' is not a command` | Docker Compose v1 vs v2 | Use `docker compose` (v2, with space) not `docker-compose`; ensure Docker Desktop updated |

### 3.3 Docker Compose Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `docker compose up -d` fails | YAML syntax error; wrong indentation | Validate YAML: `docker compose config`; check indentation (2-space); verify `version: '3.8'` |
| `Service 'mysql' failed to build` | No build context; wrong image name | Use `image: mysql:8.0` (pull existing); or ensure `build:` context correct |
| `Rebuilding app service` | Code changes detected; force rebuild | Use `--no-build` flag; or add `pull: always`; clear cache: `docker system prune` |
| `Ports are not valid` | Port mapping format error | Use `"host:container"` format: `"8080:8080"`; verify no conflicts |
| `depends_on not waiting for MySQL` | Health check not configured | Add `healthcheck` to MySQL service; or use `wait-for-it.sh` script |
| `Environment variable expansion failed` | Variable references in wrong place | Use `${VAR}` or `${VAR:-default}` syntax correctly; avoid quoting issues |
| `Named volume "mysql_data" not found` | Volume not created; first run issue | `docker compose up -d` creates volumes automatically; or `docker volume create mysql_data` |

### 3.4 Container Health Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Health check failed` | App not ready; DB not ready; endpoint wrong | Wait for app: `sleep 30`; check `docker compose logs`; verify `/actuator/health` endpoint |
| `Container status: Up (unhealthy)` | Health check too aggressive; timeout too short | Increase `start_period`, `interval`, `retries` in healthcheck config |
| `Health check timeout` | App slow to start; heavy initialization | Increase `timeout`; add `test: ["CMD-SHELL", "curl -f ..."]`; optimize startup |
| `Health check passes but app unresponsive` | Endpoint returns 200 but app broken | Add custom health check; verify actual app functionality (try login, create complaint) |
| `Health check for MySQL fails` | MySQL still starting up; `mysqladmin ping` not ready | Increase MySQL health check retries/interval; use `wait-for-it.sh` | 

## 4. Jenkins Issues

### 4.1 Pipeline Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Jenkins pipeline stuck` | Endless loop; hanging stage | Check build log; `Ctrl+C` to stop; inspect which stage hanging; increase timeout |
| `Stage 3 (compile) fails` | Code errors; Maven issues | Run `./mvnw clean compile` locally first; fix errors; commit fix |
| `Stage 4 (unit tests) fails` | Test failures; wrong assertions | Run tests locally: `./mvnw test`; fix failing tests; check assertions |
| `Stage 5 (integration tests) fails` | H2/DB config issue; profile mismatch | Ensure `@ActiveProfiles("test")`; verify `spring.profiles.active` |
| `Stage 8 (Docker build) fails` | Dockerfile issues; build context | Build Docker image locally first; fix Dockerfile; check `.dockerignore` |
| `Stage 9 (Selenium tests) fails` | Browser not found; selector issues | Run Selenium locally with Chrome; fix element selectors; use headless mode |
| `Stage 11 (deploy) skipped` | Quality gate earlier stage failed | Previous stage (9) failed; fix tests; pipeline correctly blocks deployment |
| `Pipeline times out` | Stage takes too long; hanging process | Increase timeout in Jenkins configure; check for hanging processes; optimize scripts |

### 4.2 Jenkins Configuration Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Cannot connect to Jenkins` | Jenkins not running; port conflict | Start Jenkins (`java -jar jenkins.war --httpPort=8080`); check `http://localhost:8080` |
| `GitHub webhook not triggering` | Wrong URL; secret mismatch; firewall | Verify webhook URL: `http://<jenkins>:8080/github-webhook/`; match secret in Jenkins credentials |
| `Jenkins credentials not found` | Credentials not added to Jenkins | `Manage Jenkins → Manage Credentials → Add Credentials`; ID must match `Jenkinsfile` references |
| `Pipeline not found` | Job not configured | Create new Jenkins Pipeline job; paste `Jenkinsfile` content; select "Pipeline script from SCM" |
| `Jenkins out of memory` | Too many builds; large artifacts | Increase JVM memory for Jenkins; archive old builds; clean workspace periodically |
| `Git polling not working` | Credentials issue; repository URL wrong | Verify Git URL; test SSH connection; check GitHub token permissions |
| `Jenkins not running after restart` | Service not configured; port in use | `sudo systemctl restart jenkins`; check port conflict; reinstall if needed |

### 4.3 Docker/Registry Issues in Jenkins

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Docker login fails` | Wrong credentials; registry URL incorrect | Verify `docker-hub-credentials` in Jenkins; test `docker login` manually |
| `Docker push fails` | Image not built; wrong tag; registry permissions | Ensure stage 8 builds image; verify tag format; check Docker Hub quota/rate limits |
| `Docker image not found in registry` | Wrong repository name; tag not pushed | Check `REGISTRY/${IMAGE_NAME}:${IMAGE_TAG}`; verify push completed; list images on Docker Hub |
| `Jenkins can't pull image` | Registry auth; network issues | Verify credentials; check network connectivity; test `docker pull` manually |
| `Registry rate limit exceeded` | Too many pulls/pushes from same IP | Upgrade Docker Hub account; use personal access token; reduce build frequency |

## 5. Ansible Issues

### 5.1 Playbook Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Unreachable host` | SSH access denied; wrong IP/credentials | Verify `inventory.yml`; test SSH manually: `ssh user@host`; check firewall |
| `Permission denied (publickey,password)` | SSH key not authorized; password wrong | Use `--ask-pass`; generate new SSH key; add to `authorized_keys` on target |
| `Package install failed` | Repo not accessible; wrong package name | `apt-get update` first; check package names; `apt-cache search` |
| `Docker not found` | Docker not installed; PATH issue | Run install role: `ansible-playbook -i inventory.yml site.yml --tags "docker"` |
| `Swap not disabled` | Swap still enabled; Docker fails to start | `swapoff -a`; edit `/etc/fstab` to remove swap line; re-run playbook |
| `Playbook fails mid-way` | Earlier task error; `fail fast` | Fix first error; re-run playbook (Ansible resumes from where it failed) |
| `Variable substitution failed` | Jinja2 template syntax error | Check `.j2` files; ensure `{{ variable }}` syntax; verify variables defined |
| `Idempotency not working` | Task changes state each run | Review task; use idempotent modules; add `changed_when: false`; debug with `--diff` |

### 5.2 Ansible Connection Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Connection time out` | Firewall blocking SSH; SSH not listening | `ufw status`; open port 22; `service ssh restart`; check AWS Security Groups |
| `Python version not supported` | Ansible requires Python 3 | `ansible_python_interpreter=/usr/bin/python3` in inventory; install Python 3 on target |
| `No module named docker` | Docker Python module not installed | `pip install docker`; or use `community.docker` collection; re-run playbook |
| `SSH key permission too open` | `~/.ssh/id_rsa` permissions too permissive | `chmod 600 ~/.ssh/id_rsa`; `chmod 644 ~/.ssh/id_rsa.pub` |
| `Target host not running` | Server rebooted; service stopped | `ssh user@host`; start services; `systemctl start docker`; `systemctl start nginx` |

### 5.3 Idempotency Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `changed=0 not achieved on second run` | Non-idempotent tasks; file timestamps; package state | Review each task; use `ansible.builtin.file` with `state: present`; use idempotent modules; `--diff` to see changes |
| `File modified each run` | `copy` module with `backup: yes`; `template` with changed content | Use `delimiter: '|'`; or `force: no` when content unchanged; configure `mode` explicitly |
| `Service restarted each run` | `service` module detects state change | Use `state: started`; add `enabled: yes`; use `no_restart: true` if available; use `systemd` unit flags |
| `Apt-get update each run` | `apt` module without cache | `apt-get update` only when needed; use `apt::update` with `only_if_changed`; cache valid time |

## 6. Security Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `Password found in source code` | BCrypt hash stored incorrectly; hardcoded password | Search for plain text `password`; replace with BCrypt-encoded value; use `PasswordEncoder`; check `.gitignore` |
| `Default admin credentials` | `admin/admin123` still in use | Change default password on first login; document in README; never commit real credentials |
| `BCrypt hash mismatch` | Wrong strength; encoding issue | Use `BCryptPasswordEncoder` with strength 10; verify with `PasswordEncoder.matches()` |
| `CSRF tokens not matching` | Spring Security CSRF misconfiguration | Ensure `@EnableWebSecurity`; add CSRF token to forms via `{{ csrfToken }}`; use `th:action` correctly |
| `Unencrypted credentials in .env` | `.env` committed to Git | Ensure `.env` in `.gitignore`; never commit real passwords; use `.env.example` with placeholders |
| `Security headers missing` | Spring Security not configured | Add security filter chain; configure `X-Content-Type-Options`, `X-Frame-Options`, `Content-Security-Policy` |

## 7. Testing Issues

### 6.1 Selenium Test Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `WebDriverException: chrome not found` | ChromeDriver version mismatch; Chrome not installed | Ensure Chrome browser installed; matching `chromedriver` version; use `webdriver-manager` Maven plugin |
| `SessionNotFoundException` | Browser closed unexpectedly; test timeout | Increase `WebDriverWait` timeout; check for JS errors; handle modals/popups |
| `ElementNotFoundException` | Selector changed; element not in DOM | Use more robust selector; add `data-testid` attributes; use `By.name`/`By.id` preferred |
| `Timeout waiting for element` | Page load slow; element not yet rendered | Increase `WebDriverWait` duration; add `ExpectedConditions.visibilityOfElementLocated`; ensure page fully loads |
| `Selenium tests pass locally but fail in Jenkins` | No display server; headless mode not configured | Use `ChromeOptions` with `--headless --no-sandbox --disable-dev-shm-usage`; set `MAVEN_OPTS` |
| `Selenium tests very slow` | `Thread.sleep()` used; implicit waits too long | Replace `Thread.sleep()` with `WebDriverWait`; use `implicitlyWait(5, SECONDS)` max; use expected conditions |
| `Test flakiness (sometimes passes, sometimes fails)` | Race conditions; non-deterministic UI state | Use explicit waits; stabilize test data; reset state between tests; use `@BeforeEach` to clean up |
| `Browser console errors not visible` | Logs not captured | Add `driver.manage().logs().get(LogType.BROWSER);`; configure `LogCollector`; check Jenkins console output |

### 6.2 Test Execution Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `./mvnw test fails` | Test compilation error; missing dependencies | Run specific test: `./mvnw test -Dtest=ComplaintServiceTest`; check classpath; fix compilation |
| `Test class not found` | Wrong test class name; surefire includes/excludes | Check `**/*Test.java` pattern; ensure test classes end with `Test`; configure surefire plugin |
| `Test reports not generated` | Surefire output directory issue | Check `target/surefire-reports/`; ensure Surefire plugin configured in `pom.xml`; run with `-Dsureire.useFile=false` |
| `Tests conflict with each other` | Shared state; database not cleaned | Use `@BeforeEach` / `@AfterEach`; use H2 in-memory with fresh DB per test; transaction rollback |
| `OutOfMemoryError during tests` | Too many test instances; memory leak | Reduce test count; increase `MAVEN_OPTS `-Xmx1024m; ensure `@AfterClass` cleans up resources |
| `Integration tests fail with prod profile` | Wrong `spring.profiles.active` | Use `@ActiveProfiles("test")`; or `-Dspring.profiles.active=test` on command line |
| `AssertionError: expected <> but was <> ` | Test assertion incorrect | Fix assertion; check actual vs expected; add informative message to assert; debug with System.out |

### 6.3 Mockito Issues

| Symptom | Likely Cause | Solution |
|---------|--------------|----------|
| `MockitoException: cannot instantiate @Spy` | Wrong usage of `@Spy`; constructor issues | Use default constructor; or `@InjectMocks`; check Spy object creation pattern |
| `NullPointerException with Mock` | Mock not initialized; `@Mock` missing | Use `@RunWith(MockitoExtension.class)`; or `@ExtendWith(MockitoExtension.class)`; call `MockitoAnnotations.openMocks(this)` |
| `UnfinishedStubbingException` | Missing `when()` stub; mock called without stubbing | Add `when(mock.method()).thenReturn(value);`; verify mock is stubbed for all call patterns |
| `MockitoVersionException` | Mockito version incompatibility | Use compatible Mockito version with Spring Boot 3.x; `org.mockito:mockito-core:5.2.0` or later; `spring-boot-starter-test` manages version |
| `MockedConstruction not supported` | Java version; Mockito version | Use Mockito 5.5+ for `mockedConstruction`; check Java 21 compatibility; upgrade Mockito |

## 8. General Solutions

### 8.1 Clear Cache and Restart

```bash
# Clear Maven cache (use with caution)
./mvnw clean 

# Clear Surefire test results
rm -rf target/surefire-reports

# Clear Docker images (warning: removes images)
docker system prune -a

# Clear Docker volumes (warning: removes data)
docker volume prune

# Restart Docker Compose
docker compose down
docker compose up -d

# Clear Ansible facts (re-run to refresh)
ansible -i inventory.yml all -m setup  # re-gather facts
```

### 82. Log Locations

| Component | Log Location |
|-----------|-------------|
| **Maven/Surefire** | `target/surefire-reports/*.xml` |
| **Application** | `docker compose logs -f app` or `java -jar` console output |
| **MySQL** | `docker compose logs -f mysql` or `/var/log/mysql/mysql.log` |
| **Docker** | `docker logs <container_id>` |
| **Nginx** | `docker compose logs -f nginx` or `/var/log/nginx/access.log` |
| **Jenkins** | Jenkins UI → Build → Console Output; `/var/log/jenkins/jenkins.log` |
| **Ansible** | `ansible-playbook -i inventory.yml site.yml --vvv` (very verbose) |
| **Spring Boot Actuator** | `http://localhost:8080/actuator/logs` (if configured) |

### 8.3 Getting Unstuck

1. **Read the error message carefully** - often contains solution hints
2. **Google the exact error** - include version numbers (Java 21, Maven 3.9, Docker 28.5)
3. **Check recent changes** - what did you last change? revert if needed
4. **Run in isolation** - test the component alone (e.g., `./mvnw test` without Selenium)
5. **Ask for help** - show error messages, what you've tried, environment details
6. **Consult documentation** - this troubleshooting guide, README, Wiki

### 8.4 Environment Reset (Complete Fresh Start)

```bash
# 1. Stop and remove all Docker containers/compose
docker compose down -v  # -v removes anonymous volumes

# 2. Remove Docker images (optional)
docker rmi $(docker images -q) 

# 3. Remove H2 database files (if in project)
rm -f ./target/h2*.mv.db  # or whatever H2 creates

# 4. Clean Maven build artifacts
rm -rf target/
# OR: ./mvnw clean

# 5. Reset Git to last clean commit (if needed)
git reset --hard HEAD
git pull origin main

# 6. Restart fresh
docker compose up -d  # brings up MySQL + app fresh
./mvnw flyway:migrate  # fresh schema
./mvnw test  # run tests fresh
```

---