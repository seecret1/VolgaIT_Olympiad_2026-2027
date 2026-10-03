package io.github.seecret1.volgait.driver;

import io.github.seecret1.volgait.config.TestConfig;
import org.openqa.selenium.Dimension;
import org.openqa.selenium.MutableCapabilities;
import org.openqa.selenium.PageLoadStrategy;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.chrome.ChromeDriver;
import org.openqa.selenium.chrome.ChromeOptions;
import org.openqa.selenium.edge.EdgeDriver;
import org.openqa.selenium.edge.EdgeOptions;
import org.openqa.selenium.firefox.FirefoxDriver;
import org.openqa.selenium.firefox.FirefoxOptions;
import org.openqa.selenium.remote.RemoteWebDriver;
import org.openqa.selenium.safari.SafariDriver;
import org.openqa.selenium.safari.SafariOptions;

import java.net.MalformedURLException;
import java.net.URI;
import java.time.Duration;
import java.util.Locale;

public final class DriverFactory {
    private static final String DISABLE_POPUPS = "--disable-popup-blocking";
    private static final String DISABLE_NOTIFICATIONS = "--disable-notifications";
    private static final String WINDOW_SIZE = "--window-size=1440,1000";
    private static final String HEADLESS = "--headless=new";
    private static final String NO_SANDBOX = "--no-sandbox";
    private static final String DISABLE_SHARED_MEMORY = "--disable-dev-shm-usage";
    private static final Dimension WINDOW = new Dimension(1440, 1000);

    private DriverFactory() {
    }

    public static WebDriver create() {
        BrowserType browser = BrowserType.from(TestConfig.browser());
        MutableCapabilities options = optionsFor(browser);
        WebDriver driver = TestConfig.remoteUrl().isEmpty() ? switch (browser) {
            case CHROME -> new ChromeDriver((ChromeOptions) options);
            case FIREFOX -> new FirefoxDriver((FirefoxOptions) options);
            case EDGE -> new EdgeDriver((EdgeOptions) options);
            case SAFARI -> new SafariDriver((SafariOptions) options);
        } : remoteDriver(options);

        try {
            driver.manage().timeouts().implicitlyWait(Duration.ZERO);
            driver.manage().timeouts().pageLoadTimeout(TestConfig.pageLoadTimeout());
            driver.manage().window().setSize(WINDOW);
            return driver;
        } catch (RuntimeException exception) {
            try {
                driver.quit();
            } catch (RuntimeException cleanupFailure) {
                exception.addSuppressed(cleanupFailure);
            }
            throw exception;
        }
    }

    static MutableCapabilities optionsFor(BrowserType browser) {
        return switch (browser) {
            case CHROME -> chromeOptions();
            case FIREFOX -> firefoxOptions();
            case EDGE -> edgeOptions();
            case SAFARI -> safariOptions();
        };
    }

    private static WebDriver remoteDriver(MutableCapabilities options) {
        try {
            return new RemoteWebDriver(URI.create(TestConfig.remoteUrl()).toURL(), options);
        } catch (MalformedURLException exception) {
            throw new IllegalArgumentException("remoteUrl must be a valid Selenium Grid URL", exception);
        }
    }

    private static ChromeOptions chromeOptions() {
        ChromeOptions options = new ChromeOptions();
        if (!TestConfig.browserBinary().isEmpty()) {
            options.setBinary(TestConfig.browserBinary());
        }
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addArguments(DISABLE_POPUPS, DISABLE_NOTIFICATIONS, WINDOW_SIZE);
        if (TestConfig.headless()) {
            options.addArguments(HEADLESS, NO_SANDBOX, DISABLE_SHARED_MEMORY);
        }
        return options;
    }

    private static FirefoxOptions firefoxOptions() {
        FirefoxOptions options = new FirefoxOptions();
        if (!TestConfig.browserBinary().isEmpty()) {
            options.setBinary(TestConfig.browserBinary());
        }
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        options.addPreference("dom.disable_open_during_load", false);
        options.addPreference("dom.webnotifications.enabled", false);
        if (TestConfig.headless()) {
            options.addArguments("-headless");
        }
        return options;
    }

    private static EdgeOptions edgeOptions() {
        EdgeOptions options = new EdgeOptions();
        options.setPageLoadStrategy(PageLoadStrategy.EAGER);
        if (!TestConfig.browserBinary().isEmpty()) {
            options.setBinary(TestConfig.browserBinary());
        }
        options.addArguments(DISABLE_POPUPS, DISABLE_NOTIFICATIONS, WINDOW_SIZE);
        if (TestConfig.headless()) {
            options.addArguments(HEADLESS, NO_SANDBOX, DISABLE_SHARED_MEMORY);
        }
        return options;
    }

    private static SafariOptions safariOptions() {
        if (TestConfig.headless()) {
            throw new IllegalArgumentException("Safari does not support headless mode. Set -Dheadless=false.");
        }
        if (!TestConfig.browserBinary().isEmpty()) {
            throw new IllegalArgumentException("Safari does not support browserBinary.");
        }
        if (TestConfig.remoteUrl().isEmpty()
                && !System.getProperty("os.name").toLowerCase(Locale.ROOT).contains("mac")) {
            throw new IllegalArgumentException("Local Safari requires macOS. Use remoteUrl for a Mac Grid node.");
        }
        return new SafariOptions();
    }
}
