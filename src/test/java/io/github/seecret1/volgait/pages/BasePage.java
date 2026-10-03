package io.github.seecret1.volgait.pages;

import io.github.seecret1.volgait.config.TestConfig;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.Keys;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;
import org.openqa.selenium.support.PageFactory;

import java.util.List;

public abstract class BasePage {
    protected final WebDriver driver;
    protected final WebDriverWait wait;

    @FindBy(css = "h1[itemprop='headline']")
    private WebElement heading;

    protected BasePage(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.timeout());
        PageFactory.initElements(driver, this);
    }

    protected WebElement visible(WebElement element) {
        return wait.until(ExpectedConditions.visibilityOf(element));
    }

    protected WebElement clickable(WebElement element) {
        return wait.until(ExpectedConditions.elementToBeClickable(element));
    }

    protected List<WebElement> all(List<WebElement> elements) {
        return wait.until(current -> elements.isEmpty() ? null : elements);
    }

    protected void replace(WebElement element, String value) {
        element = visible(element);
        element.sendKeys(Keys.chord(Keys.CONTROL, "a"), value);
    }

    protected String pageHeading() {
        return visible(heading).getText().trim();
    }

    protected void scrollIntoView(WebElement element) {
        ((JavascriptExecutor) driver).executeScript(
                "arguments[0].scrollIntoView({block: 'center'});", visible(element));
    }
}
