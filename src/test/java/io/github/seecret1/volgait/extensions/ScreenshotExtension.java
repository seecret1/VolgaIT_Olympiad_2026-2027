package io.github.seecret1.volgait.extensions;

import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestWatcher;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.function.Supplier;

public final class ScreenshotExtension implements TestWatcher {
    private final Supplier<WebDriver> driverSupplier;

    public ScreenshotExtension(Supplier<WebDriver> driverSupplier) {
        this.driverSupplier = driverSupplier;
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        WebDriver driver = driverSupplier.get();
        if (driver == null) {
            return;
        }

        if (driver instanceof TakesScreenshot screenshotDriver) {
            byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
            Allure.addAttachment("Screenshot on failure", "image/png",
                    new ByteArrayInputStream(screenshot), ".png");
        }

        Allure.addAttachment("Page source", "text/html", driver.getPageSource(), ".html");
        Optional.ofNullable(cause.getMessage())
                .ifPresent(message -> Allure.addAttachment("Failure", message));
    }
}

