# Testing Strategy

## 1. Testing Pyramid

The testing strategy follows a testing pyramid approach, with a broad base of unit tests, a middle layer of integration tests, and a small top layer of end-to-end UI tests.

```
        +------------------+
        |   End-to-End     |
        |   (Selenium UI)  |
        +--------+---------+
                 |
        +--------+---------+
        |  Integration     |
        |  (Spring Boot Test)|
        +--------+---------+
                 |
        +--------+---------+
        |   Unit Tests     |
        |  (JUnit 5 + Mockito)|
        +------------------+
```

**Ratio**: 70% unit tests, 20% integration tests, 10% end-to-end (Selenium)

## 2. Unit Tests (JUnit 5 + Mockito)

### 2.1 Scope

Unit tests test individual components in isolation:

| Component | Tested With | Mocked Dependencies |
|-----------|-------------|---------------------|
| **UserRepository** | JUnit 5 + Mockito | Spring Data JPA, database |
| **ComplaintService** | JUnit 5 + Mockito | ComplaintRepository, UserService |
| **StatusHistoryService** | JUnit 5 + Mockito | StatusHistoryRepository |
| **AuthService** | JUnit 5 + Mockito | UserDetailsService, PasswordEncoder |
| **GlobalExceptionHandler** | JUnit 5 | Spring MVC, WebContext |

### 2.2 Unit Test Example (ComplaintService)

```java
@ExtendWith(SpringExtension.class)
@SpringBootTest
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ComplaintService complaintService;

    @Test
    void shouldCreateComplaintWithValidData() {
        // Arrange
        User tester = new User();
        tester.setUsername("citizen1");
        tester.setRole(Role.CITIZEN);
        when(userRepository.findByUsername("citizen1")).thenReturn(Optional.of(tester));

        Complaint validComplaint = new Complaint();
        validComplaint.setStreetName("Main St");
        validComplaint.setWard("West Ward");
        validComplaint.setFaultCategory(FaultCategory.NOT_WORKING);
        validComplaint.setDescription("Streetlight not working");
        validComplaint.setPriority(Priority.MEDIUM);

        // Act
        when(complaintService.submitComplaint(validComplaint, tester)).thenReturn(validComplaint);

        // Assert
        Complaint result = complaintService.submitComplaint(validComplaint, tester);
        assertNotNull(result.getComplaintId());
        assertEquals("Main St", result.getStreetName());
        assertEquals(REPORTED, result.getStatus());
    }

    @Test
    void shouldThrowExceptionWhenMissingRequiredFields() {
        // Arrange - complaint with null required fields
        Complaint invalidComplaint = new Complaint();
        // streetName, ward, etc. are null

        // Act & Assert
        assertThrows(IllegalArgumentException.class,
            () -> complaintService.submitComplaint(invalidComplaint, null));
    }
}
```

### 2.3 Unit Test Checklist

- [ ] Test class named appropriately (e.g., `ComplaintServiceTest`)
- [ ] Test methods named using convention (e.g., `shouldCreateComplaintWithValidData`)
- [ ] @BeforeEach or @SetupUsed for shared setup
- [ ] @AfterEach or @TearDownUsed for cleanup (if needed)
- [ ] Assertions verify expected behavior, not just "no exception"
- [ ] Test data is self-contained (no reliance on test order)
- [ ] Negative test cases included (invalid inputs, edge cases)
- [ ] Tests run via `./mvnw test` successfully

## 3. Integration Tests (Spring Boot Test)

### 3.1 Scope

Integration tests test components together with the Spring context, using an in-memory database (H2):

| Integration Test | Description |
|------------------|-------------|
| **Context Loads** | `SpringBootTest` confirms application context starts |
| **REST Endpoints** | `TestRestTemplate` or `MockMvc` tests HTTP endpoints |
| **Database Operations** | Repository methods work with H2 in-memory DB |
| **Security Config** | Authentication/authorization works in test context |
| **Flyway Migrations** | Schema created correctly for tests |

### 3.2 Integration Test Example (Complaint Controller)

```java
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")  // uses H2 database
class ComplaintIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ComplaintRepository complaintRepository;

    @BeforeEach
    void setUp() {
        // Create test user before each test
        User admin = new User();
        admin.setUsername("admin");
        admin.setPassword("$2a$10$N.zQv3jZ9p5qK5qK5qK5qeS./GjWzF.l.uVwYgQ.Fl8="); // BCrypt hash of "admin123"
        admin.setRole(Role.ADMIN);
        userRepository.save(admin);
    }

    @Test
    @WithMockUser(username = "admin", roles = {"ADMIN"})
    void shouldCreateComplaint_whenAdminSubmits() throws Exception {
        // Arrange
        String json = """{
            "streetName": "Oak Ave",
            "ward": "East Ward",
            "faultCategory": "NOT_WORKING",
            "description": "Flickering streetlight",
            "priority": "HIGH"
        }""";

        // Act & Assert
        mockMvc.perform(post("/complaints")
                .contentType(MediaType.APPLICATION_JSON)
                .content(json))
            .andExpect(status().isCreated())
            .andExpect(jsonPath("$.streetName").value("Oak Ave"))
            .andExpect(jsonPath("$.status").value("REPORTED"));
    }

    @Test
    @WithMockUser(username = "citizen", roles = {"CITIZEN"})
    void shouldNotAllowCitizenToCreateComplaintWithAdminFields() throws Exception {
        // Citizen should still be able to create, but with citizen-limited fields
        // This tests that role doesn't block basic complaint submission
    }
}
```

### 3.3 Integration Test Checklist

- [ ] `@SpringBootTest` annotation present
- [ ] `@AutoConfigureMockMvc` or `TestRestTemplate` used
- [ ] `@ActiveProfiles("test")` ensures H2 database
- [ ] `@BeforeEach` or `@BeforeAll` sets up test data
- [ ] Tests independent (no ordering reliance)
- [ ] Both positive (happy path) and negative tests included
- [ ] HTTP status codes verified (200, 201, 400, 401, 403, 404)
- [ ] JSON response fields verified where applicable
- [ ] Tests pass via `./mvnw test`

## 4. End-to-End Tests (Selenium WebDriver)

### 4.1 Scope

Selenium tests test complete user journeys through the actual browser UI:

| User Journey | Test ID | Actions Asserted |
|--------------|---------|------------------|
| **1** | TG-1 | User login and logout |
| **2** | TG-2 | Successful complaint submission |
| **3** | TG-3 | Search for existing complaint |
| **4** | TG-4 | Authorized status update |
| **5** | TG-5 | Unauthorized access restriction |

### 4.2 Selenium Test Design Guidelines

#### Element Selectors (Prefer IDs or data-testid)

```html
<!-- Good: IDs are stable and unique -->
<input type="text" id="streetName" th:field="*{streetName}" required />

<!-- Good: data-testid for UI testing -->
<button data-testid="submit-complaint" type="submit">Submit</button>

<!-- Acceptable: Bootstrap class names (but may change) -->
<button type="submit" class="btn btn-primary">Submit</button>

<!-- Avoid: XPath like //div[3]/button (fragile) -->
```

#### Wait Strategies

```java
// Good: Explicit wait (WebDriverWait) with expected conditions
WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
WebElement element = wait.until(ExpectedConditions.presenceOfElementLocated(By.id("complaintStatus")));

// Bad: Arbitrary sleep (unreliable, slows tests)
Thread.sleep(2000);  // avoid this

// Best: Waiting for specific condition (element clickable, visible, etc.)
WebElement button = wait.until(ExpectedConditions.elementToBeClickable(By.id("submit")));
button.click();
```

#### Test Structure (JUnit 5 + Selenium)

```java
@ExtendWith(SeleniumExtension.class)  // custom extension or use SeleniumManager
class ComplaintSeleniumTests {

    @RegisterWebDriver  // or @Factory, depending on setup
    private WebDriver driver;

    @Test
    void shouldLoginAndLogout() {
        // Given: user on login page
        driver.get("http://localhost:8080/login");
        
        // When: user enters valid credentials and logs in
        driver.findElement(By.name("username")).sendKeys("admin");
        driver.findElement(By.name("password")).sendKeys("admin123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();
        
        // Then: user redirected to dashboard and logged in
        assertTrue(driver.getCurrentUrl().contains("/dashboard"));
        
        // And: user logs out
        driver.findElement(By.linkText("Logout")).click();
        assertTrue(driver.getCurrentUrl().contains("/login") || driver.getCurrentUrl().contains("/"));
    }
}
```

### 4.3 Selenium Test Checklist

- [ ] 5 user journeys covered (TG-1 through TG-5)
- [ ] Positive test cases (happy path) for each journey
- [ ] Negative test cases (invalid data, unauthorized, etc.) for each journey
- [ ] Element selectors use IDs or `data-testid` attributes
- [ ] Explicit `WebDriverWait` used (no `Thread.sleep` except in exceptional cases)
- [ ] Failure screenshots captured when tests fail (using `TakesScreenshot`)
- [ ] Tests run via `./mvnw test` or Maven Surefire plugin
- [ ] ChromeDriver version matches installed Chrome browser
- [ ] Headless mode configured for CI (see below)
- [ ] Test assertions are meaningful with clear error messages

### 4.4 Headless Browser Configuration for CI

When running Selenium tests in Jenkins (no display server), configure headless Chrome:

```java
// In test configuration or @BeforeClass
ChromeOptions options = new ChromeOptions();
options.addArguments("--headless");       // run without GUI
options.addArguments("--no-sandbox");     // needed for Docker/CI
options.addArguments("--disable-dev-shm-usage"); // memory sharing
options.setCapability("browserName", "chrome");
options.setCapability("chromeOptions", options);

WebDriver driver = new ChromeDriver(options);
```

**Maven Surefire configuration** (in pom.xml) for headless:
```xml
<plugin>
    <groupId>org.apache.maven.plugins</groupId>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <argLine>
            -Djava.awt.headless=true
            -Xmx512m
        </argLine>
    </configuration>
</plugin>
```

### 4.5 Selenium Test Execution

```bash
# Run all Selenium tests
./mvnw test -Dtest="*SeleniumTest"

# Run specific test
./mvnw test -Dtest="SeleniumLoginTest"

# Run with headless (CI)
MAVEN_OPTS="-Dorg.openqa.chrome.driver=chromedriver" ./mvnw test

# Generate test reports (Surefire produces XML in target/surefire-reports)
```

### 4.6 Test Data Management

- **Controlled test data**: create test users/complaints in @BeforeEach or @BeforeAll
- **Database cleanup**: use `@AfterEach` to roll back or clean up test data
- **H2 database**: integration and Selenium tests use H2 (not production MySQL)
- **Test user credentials**: 
  - Citizen: `citizen1` / `password1`
  - Technician: `technician1` / `password1`
  - Admin: `admin` / `admin123` (temporary, change after first login)
- **No real personal data**: use fictional data only

## 5. Test Execution Order

| Level | Command | Description |
|-------|---------|-------------|
| **Unit** | `./mvnw test` | Runs all JUnit 5 + Mockito tests |
| **Integration** | `./mvnw test -Dspring.profiles.active=test` | Runs with H2 profile |
| **Selenium** | `./mvnw test -Dtest="*SeleniumTest"` | Runs Selenium WebDriver tests |
| **All** | `./mvnw test` | Runs all test levels (unit + integration + Selenium if configured) |

**Maven Surefire configuration** (pom.xml) to include all test types:
```xml
<plugin>
    <artifactId>maven-surefire-plugin</artifactId>
    <configuration>
        <includes>
            <include>**/*Test.java</include>
            <include>**/*IT.java</include>
            <include>**/*SeleniumTest.java</include>
        </includes>
    </configuration>
</plugin>
```

## 6. Quality Gates in Jenkins

### 6.1 Pipeline Quality Gates

| Stage | Gate Condition | Action on Failure |
|-------|----------------|-------------------|
| **Stage 4** (Unit Tests) | All JUnit tests pass | Pipeline stops; no further stages run |
| **Stage 5** (Integration Tests) | All integration tests pass | Pipeline stops; no Docker build |
| **Stage 9** (Selenium Tests) | All 5 user journey tests pass | Pipeline stops; no deployment to production |
| **Stage 11** (Deploy Gates) | Previous quality gates all passed | Deployment only proceeds if all prior gates green |
| **Stage 12** (Health Check) | `/actuator/health` returns UP | Rollback initiated if health check fails |

### 6.2 Failed Test Blocking Deployment

```groovy
// In Jenkinsfile - Stage 9: Selenium Tests
stage('9. Run Selenium Tests') {
    when {
        expression { return currentBuild.result == null || currentBuild.result == 'SUCCESS' }
    }
    steps {
        // This stage runs only if prior stages passed
        sh './mvnw selenium:test'
        // If any Selenium test fails:
        // - currentBuild.result = 'FAILURE'
        // - Stage 10 (publish reports) still runs (to archive reports)
        // - Stage 11 (deploy) skips due to when/unless conditions
        // - Pipeline stops; no Docker image deployed
    }
    
    post {
        failure {
            // Archive screenshots and reports for debugging
            archiveArtifacts artifacts: 'screenshots/**', allowEmptyArchive: true
        }
    }
}

// Stage 11: Deploy Only If Quality Gates Pass
stage('11. Deploy Only If Quality Gates Pass') {
    when {
        expression { return currentBuild.result == 'SUCCESS' }
    }
    steps {
        // Only runs if stage 9 (Selenium) passed
        // ...
    }
}
```

### 6.3 Deliberate Defect Correction Workflow

1. **Introduce deliberate defect** (intentionally break a test):
   - Modify a test assertion to expect wrong value
   - Commit and push; Jenkins pipeline fails
   - Document the failure (screenshots, error messages)

2. **Correct the defect**:
   - Fix the test assertion to expect correct value
   - Or fix the application code that the test verifies
   - Commit the fix

3. **Successful rerun**:
   - Pipeline runs again
   - All stages pass (including Selenium)
   - Deployment proceeds
   - Document the fix and rerun results

**Documentation required**:
- Screenshots of failed test execution
- Error messages from failed test
- Description of what was wrong and how fixed
- Screenshots of successful rerun
- Pipeline build log showing all green stages

## 7. Test Evidence Requirements

For each milestone or deliverable, maintain test evidence:

| Evidence Type | Description | Storage |
|---------------|-------------|---------|
| **Selenium Screenshots** | Failure and success screenshots | `screenshots/` directory, archived in Jenkins |
| **Test Reports** | JUnit XML, surefire reports | `target/surefire-reports/`, Jenkins artifact archive |
| **Pipeline Build Logs** | Complete console output from Jenkins | Jenkins build history |
| **Database Verification** | Screenshots from MySQL/H2 client | `docs/` or `screenshots/` |
| **Code Coverage Report** | JaCoCo or similar (optional) | `target/site/jacoco/` |
| **Failed Test Artifacts** | When defect is introduced | `docs/testing-strategy.md` or `screenshots/` |

**Evidence Checklist per Milestone**:
- [ ] Unit test results (pass/fail summary)
- [ ] Integration test results
- [ ] Selenium test results (all 5 journeys)
- [ ] Screenshots for any test failures
- [ ] Pipeline build log (successful run)
- [ ] Docker image inspection (if relevant)
- [ ] Ansible idempotency verification (second run output)

---