package io.github.seecret1.volgait.pages;

import io.qameta.allure.Step;
import io.github.seecret1.volgait.components.AlertComponent;
import org.openqa.selenium.*;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;
import java.util.List;

public final class ProfilePage extends BasePage {

    private final AlertComponent alerts;
    private String lastAlertText = "";

    @FindBy(id = "userName-value")
    private WebElement userNameValue;

    @FindBy(id = "gotoStore")
    private WebElement goToStoreButton;

    @FindBy(xpath = "//button[normalize-space()='Log out' or normalize-space()='Logout']")
    private WebElement logoutButton;

    @FindBy(xpath = "//button[normalize-space()='Delete Account']")
    private WebElement deleteAccountButton;

    @FindBy(id = "closeSmallModal-ok")
    private WebElement confirmDeletionButton;

    @FindBy(css = "table tbody a[href^='/books?search=']")
    private List<WebElement> bookLinks;

    @FindBy(id = "searchBox")
    private WebElement searchBox;

    @FindBy(xpath = "//button[normalize-space()='Delete All Books']")
    private WebElement deleteAllBooksButton;

    public ProfilePage(WebDriver driver) {
        super(driver);
        alerts = new AlertComponent(driver);
    }

    public ProfilePage waitUntilLoaded() {
        new WebDriverWait(driver, Duration.ofSeconds(30))
                .until(ExpectedConditions.visibilityOf(userNameValue));
        return this;
    }

    public String displayedUserName() {
        return visible(userNameValue).getText().trim();
    }

    public String authenticationToken() {
        Object token = ((JavascriptExecutor) driver)
                .executeScript("return window.localStorage.getItem('token');");
        if (token != null && !token.toString().isBlank()) {
            return token.toString();
        }
        for (String cookieName : List.of("token", "accessToken", "authToken")) {
            Cookie cookie = driver.manage().getCookieNamed(cookieName);
            if (cookie != null && !cookie.getValue().isBlank()) {
                return cookie.getValue();
            }
        }
        return "";
    }

    public String browserStorageKeys() {
        Object keys = ((JavascriptExecutor) driver).executeScript(
                "return JSON.stringify({local:Object.keys(window.localStorage),"
                        + "session:Object.keys(window.sessionStorage)});");
        String storage = keys == null ? "{}" : keys.toString();
        String cookieNames = driver.manage().getCookies().stream()
                .map(Cookie::getName)
                .sorted()
                .toList()
                .toString();
        return storage + ", cookies=" + cookieNames;
    }

    public List<String> displayedBookTitles() {
        return driver.findElements(By.cssSelector("table tbody a[href^='/books?search=']"))
                .stream()
                .map(WebElement::getText)
                .map(String::trim)
                .filter(text -> !text.isBlank())
                .toList();
    }

    public boolean displaysBook(String title) {
        By titleLink = By.xpath("//table//a[normalize-space()="
                + xpathLiteral(title) + "]");
        return wait.until(current -> !current.findElements(titleLink).isEmpty());
    }

    @Step("Обновить профиль")
    public ProfilePage refresh() {
        driver.navigate().refresh();
        return waitUntilLoaded();
    }

    @Step("Найти книги в профиле по запросу {query}")
    public ProfilePage searchBooks(String query) {
        replace(searchBox, query);
        wait.until(current -> displayedBookTitles().stream()
                .allMatch(title -> title.toLowerCase().contains(query.toLowerCase())));
        return this;
    }

    @Step("Удалить книгу {title} из профиля")
    public ProfilePage deleteBook(String title) {
        By row = By.xpath("//tr[.//a[normalize-space()="
                + xpathLiteral(title) + "]]");
        WebElement delete = wait.until(current -> current.findElement(row)
                .findElement(By.cssSelector("span[title='Delete']")));
        scrollIntoView(delete);
        delete.click();
        confirmBookDeletion();
        wait.until(current -> current.findElements(By.linkText(title)).isEmpty());
        return this;
    }

    @Step("Удалить все книги из профиля")
    public ProfilePage deleteAllBooksAndConfirm() {
        scrollIntoView(deleteAllBooksButton);
        WebElement deleteAll = clickable(deleteAllBooksButton);
        try {
            deleteAll.click();
        } catch (ElementClickInterceptedException exception) {
            ((JavascriptExecutor) driver).executeScript("arguments[0].click();", deleteAll);
        }
        confirmBookDeletion();
        driver.navigate().refresh();
        waitUntilLoaded();
        wait.until(current -> displayedBookTitles().isEmpty());
        return this;
    }

    public ProfilePage waitForBookCount(int expectedCount) {
        TimeoutException lastError = null;
        for (int attempt = 0; attempt < 3; attempt++) {
            try {
                new WebDriverWait(driver, Duration.ofSeconds(5))
                        .until(current -> displayedBookTitles().size() == expectedCount);
                return this;
            } catch (TimeoutException exception) {
                lastError = exception;
                driver.navigate().refresh();
                waitUntilLoaded();
            }
        }
        throw lastError;
    }

    public int currentPage() {
        return paginationNumbers()[0];
    }

    public int totalPages() {
        return paginationNumbers()[1];
    }

    public ProfilePage nextPage() {
        int previousPage = currentPage();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Next']"))).click();
        wait.until(current -> currentPage() == previousPage + 1);
        return this;
    }

    public ProfilePage previousPage() {
        int previousPage = currentPage();
        wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//button[normalize-space()='Previous']"))).click();
        wait.until(current -> currentPage() == previousPage - 1);
        return this;
    }

    public boolean isNextPageEnabled() {
        return driver.findElement(By.xpath("//button[normalize-space()='Next']")).isEnabled();
    }

    public boolean isPreviousPageEnabled() {
        return driver.findElement(By.xpath("//button[normalize-space()='Previous']")).isEnabled();
    }

    @Step("Выйти из аккаунта")
    public LoginPage logout() {
        click(logoutButton);
        return new LoginPage(driver).waitUntilLoaded();
    }

    @Step("Перейти в магазин книг")
    public BookStorePage goToBookStore() {
        click(goToStoreButton);
        wait.until(ExpectedConditions.urlContains("/books"));
        return new BookStorePage(driver);
    }

    @Step("Удалить аккаунт и подтвердить удаление")
    public String deleteAccountAndConfirm() {
        scrollIntoView(deleteAccountButton);
        clickable(deleteAccountButton).click();
        WebElement confirmation = clickable(confirmDeletionButton);
        ((JavascriptExecutor) driver).executeScript("arguments[0].click();", confirmation);
        lastAlertText = alerts.acceptIfPresent(Duration.ofSeconds(10)).orElse("");
        return lastAlertText;
    }

    private void confirmBookDeletion() {
        WebElement confirmation = clickable(confirmDeletionButton);
        confirmation.click();
        lastAlertText = alerts.acceptIfPresent(Duration.ofSeconds(10)).orElse("");
    }

    public String lastAlertText() {
        return lastAlertText;
    }

    private int[] paginationNumbers() {
        By pageInfo = By.xpath(
                "//span[contains(normalize-space(),'Page ') and contains(normalize-space(),' of ')]");
        return wait.until(current -> {
            try {
                String text = current.findElement(pageInfo).getText();
                java.util.regex.Matcher matcher = java.util.regex.Pattern
                        .compile("Page\\s+(\\d+)\\s+of\\s+(\\d+)")
                        .matcher(text);
                if (!matcher.find()) {
                    return null;
                }
                return new int[]{Integer.parseInt(matcher.group(1)),
                        Integer.parseInt(matcher.group(2))};
            } catch (StaleElementReferenceException exception) {
                return null;
            }
        });
    }

    private String xpathLiteral(String value) {
        if (!value.contains("'")) {
            return "'" + value + "'";
        }
        if (!value.contains("\"")) {
            return "\"" + value + "\"";
        }
        throw new IllegalArgumentException("Book title contains both quote types: " + value);
    }
}
