package io.github.seecret1.volgait.tests;

import io.github.seecret1.volgait.config.TestConfig;
import io.github.seecret1.volgait.config.ConfigProvider;
import io.github.seecret1.volgait.driver.DriverFactory;
import io.github.seecret1.volgait.extensions.ScreenshotExtension;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.RegisterExtension;
import org.openqa.selenium.WebDriver;

public abstract class BaseUiTest implements ConfigProvider {
    protected WebDriver driver;

    @RegisterExtension
    final ScreenshotExtension screenshotExtension = new ScreenshotExtension(() -> driver);

    @BeforeEach
    void startBrowser() {
        driver = DriverFactory.create();
        Allure.parameter("browser", TestConfig.browser());
        Allure.parameter("headless", TestConfig.headless());
    }

    @AfterEach
    void stopBrowser() {
        if (driver != null) {
            driver.quit();
            driver = null;
        }
    }

    protected void open(String url) {
        driver.get(url);
    }
}
