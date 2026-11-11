package io.github.seecret1.volgait.pages;

import io.qameta.allure.Step;
import io.github.seecret1.volgait.components.AlertComponent;
import io.github.seecret1.volgait.model.BookRow;
import org.openqa.selenium.By;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URI;
import java.time.Duration;
import java.util.List;

public final class BookStorePage extends BasePage {

    private final AlertComponent alerts;

    @FindBy(css = "table tbody a[href^='/books?search=']")
    private List<WebElement> bookLinks;

    @FindBy(id = "searchBox")
    private WebElement searchBox;

    public BookStorePage(WebDriver driver) {
        super(driver);
        alerts = new AlertComponent(driver);
    }

    public BookStorePage waitUntilLoaded() {
        wait.until(ExpectedConditions.presenceOfAllElementsLocatedBy(
                By.cssSelector("table tbody a[href^='/books?search=']")));
        return this;
    }

    @Step("Открыть книгу {title}")
    public BookStorePage openBook(String title) {
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(
                By.xpath("//table//a[normalize-space()=" + xpathLiteral(title) + "]")));
        scrollIntoView(link);
        link.click();
        wait.until(ExpectedConditions.urlContains("search="));
        return this;
    }

    @Step("Открыть книгу ISBN {isbn}")
    public BookStorePage openBookByIsbn(String isbn) {
        URI current = URI.create(driver.getCurrentUrl());
        driver.get(current.resolve("/books?search=" + isbn).toString());
        wait.until(ExpectedConditions.presenceOfElementLocated(
                By.xpath("//button[normalize-space()='Add To Your Collection']")));
        return this;
    }

    @Step("Добавить открытую книгу в коллекцию")
    public String addOpenedBookToCollection() {
        By addButton = By.xpath("//button[normalize-space()='Add To Your Collection']");
        WebElement button = wait.until(ExpectedConditions.elementToBeClickable(addButton));
        click(button);
        return alerts.acceptIfPresent(Duration.ofSeconds(30)).orElse("");
    }

    @Step("Открыть профиль")
    public ProfilePage openProfile() {
        URI current = URI.create(driver.getCurrentUrl());
        driver.get(current.resolve("/profile").toString());
        return new ProfilePage(driver).waitUntilLoaded();
    }

    @Step("Найти книги по запросу {query}")
    public BookStorePage search(String query) {
        replace(searchBox, query);
        wait.until(current -> displayedBookTitles().stream()
                .allMatch(title -> title.toLowerCase().contains(query.toLowerCase())));
        return this;
    }

    public List<String> displayedBookTitles() {
        return bookLinks.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .map(String::trim)
                .filter(title -> !title.isBlank())
                .toList();
    }

    public List<BookRow> displayedBookRows() {
        return driver.findElements(By.cssSelector("table tbody tr")).stream()
                .map(row -> {
                    WebElement link = row.findElement(By.cssSelector("a[href*='search=']"));
                    WebElement image = row.findElement(By.cssSelector("img"));
                    String href = link.getAttribute("href");
                    String isbn = href.substring(href.indexOf("search=") + "search=".length());
                    return new BookRow(isbn, link.getText().trim(), image.getAttribute("src"));
                })
                .toList();
    }

    @Step("Открыть пункт левого меню {item}")
    public BookStorePage openSideMenu(String item) {
        By menuItem = By.xpath("//div[contains(@class,'left-pannel')]//*[normalize-space()="
                + xpathLiteral(item) + "]");
        WebElement link = wait.until(ExpectedConditions.elementToBeClickable(menuItem));
        scrollIntoView(link);
        link.click();
        return this;
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
