package io.github.seecret1.volgait.extensions;

import io.github.seecret1.volgait.constants.TestMetadata;
import io.qameta.allure.Allure;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.AfterTestExecutionCallback;
import org.junit.jupiter.api.extension.TestExecutionExceptionHandler;
import org.openqa.selenium.OutputType;
import org.openqa.selenium.TakesScreenshot;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.NoSuchSessionException;

import java.io.ByteArrayInputStream;
import java.util.Optional;
import java.util.function.Supplier;

public final class ScreenshotExtension implements TestExecutionExceptionHandler, AfterTestExecutionCallback {

    private final Supplier<WebDriver> driverSupplier;

    public ScreenshotExtension(Supplier<WebDriver> driverSupplier) {
        this.driverSupplier = driverSupplier;
    }

    @Override
    public void handleTestExecutionException(ExtensionContext context, Throwable cause) throws Throwable {
        if (isSessionUnavailable(cause)) {
            Allure.addAttachment("Скриншот недоступен", "text/plain",
                    "Сессия WebDriver недоступна или транспорт не отвечает; дополнительные команды пропущены, "
                            + "чтобы не скрыть первичную причину.");
        } else {
            attachBrowserState("Скриншот при ошибке");
        }
        Optional.ofNullable(cause.getMessage())
                .ifPresent(message -> Allure.addAttachment("Ошибка", message));
        throw cause;
    }

    private boolean isSessionUnavailable(Throwable cause) {
        for (Throwable current = cause; current != null; current = current.getCause()) {
            if (current instanceof NoSuchSessionException
                    || current instanceof java.util.concurrent.TimeoutException) {
                return true;
            }
        }
        return false;
    }

    @Override
    public void afterTestExecution(ExtensionContext context) {
        if (context.getExecutionException().isEmpty()
                && context.getTags().contains(TestMetadata.NEGATIVE)) {
            attachBrowserState("Состояние UI в негативном сценарии");
        }
    }

    public void attachBrowserState(String screenshotName) {
        WebDriver driver = driverSupplier.get();
        if (driver != null) {
            try {
                if (driver instanceof TakesScreenshot screenshotDriver) {
                    byte[] screenshot = screenshotDriver.getScreenshotAs(OutputType.BYTES);
                    Allure.addAttachment(screenshotName, "image/png",
                            new ByteArrayInputStream(screenshot), ".png");
                }
            } catch (RuntimeException screenshotFailure) {
                Allure.addAttachment("Скриншот недоступен", "text/plain",
                        screenshotFailure.getClass().getSimpleName() + ": "
                                + screenshotFailure.getMessage());
                return;
            }
            try {
                Allure.addAttachment("Исходный код страницы", "text/html",
                        driver.getPageSource(), ".html");
            } catch (RuntimeException pageSourceFailure) {
                Allure.addAttachment("HTML страницы недоступен", "text/plain",
                        pageSourceFailure.getClass().getSimpleName() + ": "
                                + pageSourceFailure.getMessage());
            }
        }
    }
}
