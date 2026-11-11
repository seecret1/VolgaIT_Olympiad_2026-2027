package io.github.seecret1.volgait.pages;

import io.qameta.allure.Step;
import io.github.seecret1.volgait.components.AlertComponent;
import io.github.seecret1.volgait.components.BookTableRowComponent;
import io.github.seecret1.volgait.model.BookRow;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.StaleElementReferenceException;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;

import java.net.URI;
import java.time.Duration;
import java.util.List;
import java.util.Locale;

public final class BookStorePage extends BasePage {

    private final AlertComponent alerts;

    @FindBy(css = "table tbody a[href^='/books?search=']")
    private List<WebElement> bookLinks;

    @FindBy(id = "searchBox")
    private WebElement searchBox;

    @FindBy(xpath = "//table/tbody/tr[.//a[contains(@href,'search=')]]")
    private List<WebElement> bookRows;

    @FindBy(xpath = "//button[normalize-space()='Add To Your Collection']")
    private WebElement addToCollectionButton;

    public BookStorePage(WebDriver driver) {
        super(driver);
        alerts = new AlertComponent(driver);
    }

    public BookStorePage waitUntilLoaded() {
        wait.until(current -> !bookLinks.isEmpty());
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
        visible(addToCollectionButton);
        return this;
    }

    @Step("Добавить открытую книгу в коллекцию")
    public String addOpenedBookToCollection() {
        click(addToCollectionButton);
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
        WebElement input = visible(searchBox);
        input.sendKeys(Keys.HOME, Keys.chord(Keys.SHIFT, Keys.END), Keys.BACK_SPACE);
        input.sendKeys(query);
        wait.ignoring(StaleElementReferenceException.class)
                .until(current -> query.isEmpty() ? !displayedBookTitles().isEmpty()
                        : bookRows.stream()
                        .allMatch(row -> row.getText().toLowerCase(Locale.ROOT)
                                .contains(query.toLowerCase(Locale.ROOT))));
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
        return bookRows.stream()
                .map(BookTableRowComponent::new)
                .map(row -> {
                    String href = row.href();
                    String isbn = href.substring(href.indexOf("search=") + "search=".length());
                    return new BookRow(isbn, row.title(), row.imageSource());
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
