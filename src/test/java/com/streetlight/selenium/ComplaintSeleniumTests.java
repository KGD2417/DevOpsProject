package com.streetlight.selenium;

import org.junit.jupiter.api.*;
import org.openqa.selenium.*;
import org.openqa.selenium.support.ui.*;
import static org.junit.jupiter.api.Assertions.*;
import java.time.Duration;

@TestMethodOrder(MethodOrderer.Order.class)
public class ComplaintSeleniumTests {

    @RegisterWebDriver
    WebDriver driver;

    @BeforeEach
    void setUp() {
        // Maximize browser window
        driver.manage().window().maximize();
    }

    @Test
    @Order(1)
    @DisplayName("TG-1: User login and logout")
    void testUserLoginAndLogout() {
        // Given: user on login page
        driver.get("http://localhost:8080/login");

        // When: user enters valid credentials and logs in
        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        WebElement usernameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("username")));
        WebElement passwordField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("password")));

        usernameField.sendKeys("admin");
        passwordField.sendKeys("admin123");

        WebElement loginButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        loginButton.click();

        // Then: user redirected to dashboard and logged in
        assertTrue(driver.getCurrentUrl().contains("/dashboard"),
                "Expected dashboard URL after login, got: " + driver.getCurrentUrl());

        // And: user can see dashboard elements
        WebElement dashboardTitle = wait.until(ExpectedConditions.visibilityOfElementLocated(By.tagName("h2")));
        assertTrue(dashboardTitle.isDisplayed(), "Dashboard should be visible after login");

        // And: user logs out
        WebElement logoutLink = wait.until(ExpectedConditions.elementToBeClickable(By.linkText("Logout")));
        logoutLink.click();

        // Then: user redirected to login page
        assertTrue(driver.getCurrentUrl().contains("/login") || driver.getTitle().contains("Login"),
                "Expected login page after logout");
    }

    @Test
    @Order(2)
    @DisplayName("TG-2: Successful complaint submission")
    void testSuccessfulComplaintSubmission() {
        // Given: user logged in as citizen
        driver.get("http://localhost:8080/complaints/new");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // When: user fills in the complaint form
        WebElement streetNameField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("streetName")));
        WebElement wardField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ward")));
        WebElement faultCategoryDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("faultCategory")));
        WebElement descriptionField = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("description")));
        WebElement priorityDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("priority")));

        streetNameField.sendKeys("Main Street");
        wardField.sendKeys("Downtown Ward");
        new Select(faultCategoryDropdown).selectByVisibleText("Not Working");
        descriptionField.sendKeys("Streetlight not working - reported by citizen");
        new Select(priorityDropdown).selectByVisibleText("MEDIUM");

        // And: user submits the form
        WebElement submitButton = wait.until(ExpectedConditions.elementToBeClickable(By.cssSelector("button[type='submit']")));
        submitButton.click();

        // Then: complaint submitted successfully
        assertTrue(driver.getCurrentUrl().contains("/complaints") ||
                        driver.getPageSource().contains("Complaint submitted successfully"),
                "Expected successful submission or complaints page, got: " + driver.getCurrentUrl());

        // And: complaint ID displayed
        WebElement complaintIdElement = wait.until(ExpectedConditions.presenceOfElementLocated(By.className("alert-success")));
        assertTrue(complaintIdElement.isDisplayed(), "Success alert should be displayed");
        assertTrue(complaintIdElement.getText().contains("SL-"),
                "Complaint ID should start with SL-");
    }

    @Test
    @Order(3)
    @DisplayName("TG-3: Search for existing complaint")
    void testSearchForExistingComplaint() {
        // Given: user on search page
        driver.get("http://localhost:8080/complaints");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));

        // When: user searches by ward
        WebElement wardSearch = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("ward")));
        WebElement searchButton = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@class, 'btn') and contains(., 'Search')]")));

        wardSearch.sendKeys("Downtown Ward");
        searchButton.click();

        // Then: search results displayed
        WebElement resultsTable = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(resultsTable.isDisplayed(), "Search results table should be displayed");

        // And: results filtered by ward
        boolean hasDowntownResult = driver.getPageSource().contains("Downtown Ward");
        assertTrue(hasDowntownResult, "Search results should include Downtown Ward complaints");

        // When: user clears the search
        wardSearch.clear();

        // Then: all complaints shown again
        WebElement resultsAfterClear = wait.until(ExpectedConditions.presenceOfElementLocated(By.tagName("table")));
        assertTrue(resultsAfterClear.isDisplayed(), "Results table should still be visible after clearing search");
    }

    @Test
    @Order(4)
    @DisplayName("TG-4: Authorized status update")
    void testAuthorizedStatusUpdate() {
        // Given: technician logged in
        driver.get("http://localhost:8080/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.findElement(By.name("username")).sendKeys("technician1");
        driver.findElement(By.name("password")).sendKeys("tech123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // Navigate to a complaint
        driver.get("http://localhost:8080/complaints");

        // When: technician updates status from REPORTED to ACKNOWLEDGED
        // (Assuming a complaint exists with REPORTED status)
        WebElement firstStatus = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//tr[1]//span[contains(@class, 'badge')]")));
        String initialStatus = firstStatus.getText();

        // Click on complaint to detail page
        WebElement firstComplaintLink = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//tr[1]//a")));
        firstComplaintLink.click();

        // On detail page, update status
        WebElement statusDropdown = wait.until(ExpectedConditions.presenceOfElementLocated(By.name("newStatus")));
        new Select(statusDropdown).selectByVisibleText("ACKNOWLEDGED");

        WebElement updateBtn = wait.until(ExpectedConditions.elementToBeClickable(By.xpath("//button[contains(@onclick, 'updateStatus') or contains(@type, 'submit')]")));
        updateBtn.click();

        // Then: status updated successfully
        WebElement updatedStatus = wait.until(ExpectedConditions.presenceOfElementLocated(By.xpath("//div//span[contains(@class, 'badge')]")));
        assertTrue(updatedStatus.getText().contains("ACKNOWLEDGED"),
                "Expected status ACKNOWLEDGED, got: " + updatedStatus.getText());

        // And: status history recorded
        assertTrue(driver.getPageSource().contains("Status history") ||
                        driver.getPageSource().contains("status history"),
                "Status history should be recorded");
    }

    @Test
    @Order(5)
    @DisplayName("TG-5: Unauthorized access restriction")
    void testUnauthorizedAccessRestriction() {
        // Given: citizen logged in
        driver.get("http://localhost:8080/login");

        WebDriverWait wait = new WebDriverWait(driver, Duration.ofSeconds(10));
        driver.findElement(By.name("username")).sendKeys("citizen1");
        driver.findElement(By.name("password")).sendKeys("citizen123");
        driver.findElement(By.cssSelector("button[type='submit']")).click();

        // When: citizen tries to access admin-only endpoint
        driver.get("http://localhost:8080/complaints/assign");

        // Then: access denied (403 or redirect to login)
        String currentUrl = driver.getCurrentUrl();
        String pageTitle = driver.getTitle();

        // Should not have admin-only features available
        boolean hasAdminButton = driver.getPageSource().contains("Assign Technician") ||
                                driver.getPageSource().contains("assign");
        // Citizen should not see admin-specific UI elements
        assertFalse(hasAdminButton,
                "Citizen should not see Assign Technician button, but found it in page source");
    }

    @AfterEach
    void tearDown() {
        if (driver != null) {
            driver.quit();
        }
    }
}