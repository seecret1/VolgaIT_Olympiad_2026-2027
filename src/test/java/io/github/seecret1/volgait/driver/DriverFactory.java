package io.github.seecret1.volgait.driver;

import io.github.seecret1.volgait.config.TestConfig;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;

import java.time.Duration;

public final class DriverFactory {
    private DriverFactory() {
    }

    public static WebDriver create() {
        WebDriver driver = switch (BrowserType.from(TestConfig.browser())) {
            case CHROME -> new ChromeDriver(chromeOptions());
            case FIREFOX -> new FirefoxDriver(firefoxOptions());
        };

        driver.manage().timeouts().implicitlyWait(Duration.ZERO);
        driver.manage().timeouts().pageLoadTimeout(TestConfig.pageLoadTimeout());
        driver.manage().window().setSize(new org.openqa.selenium.Dimension(1440, 1000));
        return driver;
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments("--disable-popup-blocking", "--disable-notifications", "--window-size=1440,1000");
        if (TestConfig.headless()) {
            options.addArguments("--headless=new", "--no-sandbox", "--disable-dev-shm-usage");
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addPreference("dom.disable_open_during_load", false);
        options.addPreference("dom.webnotifications.enabled", false);
        if (TestConfig.headless()) {
            options.addArguments("-headless");
        }
        return options;
    }
}

