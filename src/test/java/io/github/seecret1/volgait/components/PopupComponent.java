package io.github.seecret1.volgait.components;

import io.github.seecret1.volgait.config.TestConfig;
import io.github.seecret1.volgait.constants.DomAttributes;
import io.qameta.allure.Step;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.interactions.Actions;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.List;

public final class PopupComponent {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final WebElement root;
    private final WebElement title;
    private final WebElement content;
    private final WebElement closeButton;

    public PopupComponent(WebDriver driver, WebElement root, WebElement title,
                          WebElement content, WebElement closeButton) {
        this.driver = driver;
        this.root = root;
        this.title = title;
        this.content = content;
        this.closeButton = closeButton;
        this.wait = new WebDriverWait(driver, TestConfig.timeout());
    }

    @Step("Wait until popup is visible")
    public PopupComponent waitUntilVisible() {
        wait.until(ExpectedConditions.visibilityOf(root));
        return this;
    }

    @Step("Close popup")
    public void close() {
        wait.until(ExpectedConditions.elementToBeClickable(closeButton)).click();
        wait.until(ExpectedConditions.invisibilityOf(root));
    }

    @Step("Press Escape in popup")
    public void pressEscape() {
        new Actions(driver).sendKeys(Keys.ESCAPE).perform();
    }

    public boolean isVisible() {
        return root.isDisplayed();
    }

    public String title() {
        return wait.until(ExpectedConditions.visibilityOf(title)).getText().trim();
    }

    public String content() {
        return wait.until(ExpectedConditions.visibilityOf(content)).getText().trim();
    }

    public boolean closeButtonIsDisplayed() {
        return wait.until(ExpectedConditions.visibilityOf(closeButton)).isDisplayed();
    }

    public String role() {
        return root.getAttribute(DomAttributes.ROLE);
    }

    void replaceValue(WebElement element, String value) {
        element = wait.until(ExpectedConditions.visibilityOf(element));
        ((JavascriptExecutor) driver).executeScript("""
                const element = arguments[0];
                const value = arguments[1];
                const prototype = element instanceof HTMLTextAreaElement
                        ? HTMLTextAreaElement.prototype
                        : HTMLInputElement.prototype;
                Object.getOwnPropertyDescriptor(prototype, 'value').set.call(element, value);
                element.dispatchEvent(new Event('input', { bubbles: true }));
                element.dispatchEvent(new Event('change', { bubbles: true }));
                """, element, value);
        wait.until(ExpectedConditions.attributeToBe(element, DomAttributes.VALUE, value));
    }

    void click(WebElement element) {
        wait.until(current -> {
            if (ExpectedConditions.elementToBeClickable(element).apply(current) == null) {
                return false;
            }
            ((JavascriptExecutor) driver).executeScript(
                    "arguments[0].scrollIntoView({block: 'center', behavior: 'instant'});", element);
            try {
                element.click();
                return true;
            } catch (org.openqa.selenium.ElementClickInterceptedException exception) {
                return false;
            }
        });
    }

    String waitForNonBlankText(List<WebElement> elements) {
        return wait.until(current -> elements.stream()
                .filter(WebElement::isDisplayed)
                .map(WebElement::getText)
                .filter(text -> !text.isBlank())
                .findFirst()
                .orElse(null));
    }

}
