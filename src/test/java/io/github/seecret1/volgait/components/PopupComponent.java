package io.github.seecret1.volgait.components;

import io.qameta.allure.Step;
import org.openqa.selenium.By;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.time.Duration;

public class PopupComponent {
    private final WebDriver driver;
    private final WebDriverWait wait;
    private final By root;

    public PopupComponent(WebDriver driver, By root, Duration timeout) {
        this.driver = driver;
        this.root = root;
        this.wait = new WebDriverWait(driver, timeout);
    }

    @Step("Wait until popup is visible")
    public PopupComponent waitUntilVisible() {
        wait.until(ExpectedConditions.visibilityOfElementLocated(root));
        return this;
    }

    @Step("Close popup")
    public void close() {
        container().findElement(By.cssSelector("button.pum-close")).click();
        wait.until(ExpectedConditions.invisibilityOfElementLocated(root));
    }

    @Step("Press Escape in popup")
    public void pressEscape() {
        container().sendKeys(Keys.ESCAPE);
    }

    public boolean isVisible() {
        return driver.findElements(root).stream().anyMatch(WebElement::isDisplayed);
    }

    public String title() {
        return container().findElement(By.cssSelector(".pum-title")).getText().trim();
    }

    public String content() {
        return container().findElement(By.cssSelector(".pum-content")).getText().trim();
    }

    public boolean closeButtonIsDisplayed() {
        return container().findElement(By.cssSelector("button.pum-close")).isDisplayed();
    }

    public String role() {
        return driver.findElement(root).getAttribute("role");
    }

    public WebElement container() {
        return wait.until(ExpectedConditions.visibilityOfElementLocated(root));
    }
}

