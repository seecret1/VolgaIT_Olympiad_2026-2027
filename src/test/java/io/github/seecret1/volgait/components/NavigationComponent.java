package io.github.seecret1.volgait.components;

import io.github.seecret1.volgait.config.TestConfig;
import io.github.seecret1.volgait.constants.DomAttributes;
import io.qameta.allure.Step;
import org.openqa.selenium.JavascriptExecutor;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.FindBy;
import org.openqa.selenium.support.PageFactory;
import org.openqa.selenium.support.ui.ExpectedConditions;
import org.openqa.selenium.support.ui.WebDriverWait;

import java.util.Set;

public final class NavigationComponent {
    private final WebDriver driver;
    private final WebDriverWait wait;

    @FindBy(xpath = "//header//a[@data-hover='Blog' and .//span[normalize-space()='Blog']]")
    private WebElement blogLink;

    @FindBy(xpath = "//nav[contains(concat(' ', normalize-space(@class), ' '), ' breadcrumbs ')]//a[normalize-space()='Home']")
    private WebElement homeLink;

    @FindBy(xpath = "//*[contains(concat(' ', normalize-space(@class), ' '), ' entry-content ')]//a[contains(@href, 'youtube.com/watch')]")
    private WebElement youtubeLink;

    public NavigationComponent(WebDriver driver) {
        this.driver = driver;
        this.wait = new WebDriverWait(driver, TestConfig.timeout());
        PageFactory.initElements(driver, this);
    }

    public String blogHref() {
        return wait.until(ExpectedConditions.visibilityOf(blogLink)).getAttribute(DomAttributes.HREF);
    }

    public String homeHref() {
        return wait.until(ExpectedConditions.visibilityOf(homeLink)).getAttribute(DomAttributes.HREF);
    }

    public String youtubeHref() {
        return wait.until(ExpectedConditions.visibilityOf(youtubeLink)).getAttribute(DomAttributes.HREF);
    }

    public String youtubeTarget() {
        return wait.until(ExpectedConditions.visibilityOf(youtubeLink)).getAttribute(DomAttributes.TARGET);
    }

    @Step("Открыть Blog")
    public String openBlog() {
        return openInCurrentTab(blogLink);
    }

    @Step("Перейти на главную страницу по ссылке Home")
    public String openHome() {
        return openInCurrentTab(homeLink);
    }

    @Step("Открыть ссылку на YouTube в новой вкладке")
    public String openYoutube() {
        Set<String> initialHandles = driver.getWindowHandles();
        wait.until(ExpectedConditions.elementToBeClickable(youtubeLink)).click();
        wait.until(ExpectedConditions.numberOfWindowsToBe(initialHandles.size() + 1));

        String newHandle = driver.getWindowHandles().stream()
                .filter(handle -> !initialHandles.contains(handle))
                .findFirst()
                .orElseThrow(() -> new IllegalStateException("Новая вкладка YouTube не открылась"));
        driver.switchTo().window(newHandle);
        return wait.until(current -> {
            String url = current.getCurrentUrl();
            return url == null || url.isBlank() || "about:blank".equals(url) ? null : url;
        });
    }

    @Step("Дождаться загрузки YouTube-ролика")
    public void waitForYoutubeVideo() {
        wait.until(current -> Boolean.TRUE.equals(((JavascriptExecutor) current).executeScript("""
                const video = document.querySelector('video');
                return video !== null && video.readyState >= 1;
                """)));
    }

    private String openInCurrentTab(WebElement link) {
        String initialUrl = driver.getCurrentUrl();
        wait.until(ExpectedConditions.elementToBeClickable(link)).click();
        return wait.until(current -> initialUrl.equals(current.getCurrentUrl())
                ? null
                : current.getCurrentUrl());
    }
}
